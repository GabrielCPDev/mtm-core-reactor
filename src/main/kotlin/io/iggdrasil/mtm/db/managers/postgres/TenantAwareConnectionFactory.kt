package io.iggdrasil.mtm.db.managers.postgres

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.tenant.DataSourceType
import io.iggdrasil.mtm.tenant.TenancyDBStrategy
import io.iggdrasil.mtm.tenant.TenantContext
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.*
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class TenantAwareConnectionFactory(
    private val properties: MultiTenancyProperties
) : ConnectionFactory {

    private val log = LoggerFactory.getLogger(javaClass)

    private data class CachedPool(
        val pool: ConnectionPool,
        @Volatile var lastAccess: Long = System.currentTimeMillis()
    )

    private val pools = ConcurrentHashMap<String, CachedPool>()

    @Volatile
    private var globalPool: ConnectionPool? = null

    override fun create(): Mono<out Connection> =
        Mono.deferContextual {

            TenantContext.read()
                .defaultIfEmpty("")
                .flatMap { tenantId ->

                    val pool =
                        if (tenantId.isBlank()) {
                            log.debug("Using GLOBAL R2DBC connection")
                            getGlobalPool()
                        } else {
                            log.debug("Using TENANT R2DBC connection tenant={}", tenantId)
                            getTenantPool(tenantId)
                        }

                    Mono.from(pool.create())
                }
        }

    override fun getMetadata(): ConnectionFactoryMetadata =
        getGlobalPool().metadata

    private fun getTenantPool(tenantId: String): ConnectionPool {

        val cached = pools.computeIfAbsent(tenantId) {

            log.info("Creating R2DBC pool for tenant {}", tenantId)

            CachedPool(createPoolForTenant(tenantId))
        }

        cached.lastAccess = System.currentTimeMillis()

        return cached.pool
    }

    private fun getGlobalPool(): ConnectionPool {

        val existing = globalPool
        if (existing != null) return existing

        synchronized(this) {
            val again = globalPool
            if (again != null) return again

            log.info("Creating GLOBAL R2DBC pool")

            val created = createPool(properties.dataSource.database)
            globalPool = created
            return created
        }
    }

    /* ---------------- POOL CREATION ---------------- */

    private fun createPoolForTenant(tenantId: String): ConnectionPool {

        val database =
            when (properties.strategy) {
                TenancyDBStrategy.DATABASE ->
                    "tenant_$tenantId"

                TenancyDBStrategy.SCHEMA ->
                    properties.dataSource.database

                TenancyDBStrategy.COLLECTION ->
                    throw IllegalArgumentException("COLLECTION not supported for SQL")
            }

        return createPool(database)
    }

    private fun createPool(database: String): ConnectionPool {

        val ds = properties.dataSource

        val driver =
            when (ds.type) {
                DataSourceType.POSTGRES -> "postgresql"
                DataSourceType.MYSQL -> "mysql"
                else -> error("Invalid R2DBC type")
            }

        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, driver)
            .option(ConnectionFactoryOptions.HOST, ds.host)
            .option(ConnectionFactoryOptions.PORT, ds.port)
            .option(ConnectionFactoryOptions.DATABASE, database)
            .option(ConnectionFactoryOptions.USER, ds.username)
            .option(ConnectionFactoryOptions.PASSWORD, ds.password)
            .build()

        val factory = ConnectionFactories.get(options)

        val poolConfig = ConnectionPoolConfiguration.builder(factory)
            .maxSize(ds.maxPoolSize)
            .initialSize(2)
            .maxIdleTime(Duration.ofMinutes(30))
            .build()

        return ConnectionPool(poolConfig)
    }

    @Scheduled(fixedDelay = 300000)
    fun cleanupPools() {

        val now = System.currentTimeMillis()
        val ttl = 30 * 60 * 1000L

        pools.entries.removeIf { (tenantId, cached) ->

            val expired = now - cached.lastAccess > ttl

            if (expired) {
                log.debug("Cleaning idle pool tenant={}", tenantId)
                cached.pool.dispose()
            }

            expired
        }
    }

    @PreDestroy
    fun shutdown() {

        log.info("Shutting down TenantAwareConnectionFactory")

        pools.forEach { (tenantId, cached) ->
            try {
                log.info("Closing pool tenant={}", tenantId)
                cached.pool.dispose()
            } catch (_: Exception) {
            }
        }

        pools.clear()

        globalPool?.dispose()
        globalPool = null
    }
}
