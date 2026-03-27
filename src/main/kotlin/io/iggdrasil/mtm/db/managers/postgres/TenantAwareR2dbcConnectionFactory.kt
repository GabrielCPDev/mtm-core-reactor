package io.iggdrasil.mtm.db.managers.postgres

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.CachedDbResource
import io.iggdrasil.mtm.tenant.DataSourceType
import io.iggdrasil.mtm.tenant.TenantContext
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.*
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class TenantAwareR2dbcConnectionFactory(
    private val properties: MultiTenancyProperties
) : ConnectionFactory {

    private val log = LoggerFactory.getLogger(javaClass)

    private val pools = ConcurrentHashMap<String, CachedDbResource<ConnectionPool>>()

    val activePools: Map<String, CachedDbResource<ConnectionPool>>
        get() = pools

    @Volatile
    private var globalPool: ConnectionPool? = null

    private val baseDatabase =
        properties.dataSource.database

    override fun create(): Mono<out Connection> =
        Mono.deferContextual {
            TenantContext.read()
                .defaultIfEmpty("")
                .flatMap { tenantId ->
                    val pool = resolvePool(tenantId)
                    Mono.from(pool.create())
                }
        }

    override fun getMetadata(): ConnectionFactoryMetadata =
        getGlobalPool().metadata

    private fun resolvePool(tenantId: String): ConnectionPool =
        if (tenantId.isBlank()) {
            log.debug("Using GLOBAL R2DBC connection")
            getGlobalPool()
        } else {
            log.debug("Using TENANT R2DBC connection tenant={}", tenantId)
            getTenantPool(tenantId)
        }

    private fun getTenantPool(tenantId: String): ConnectionPool {
        val cached = pools.computeIfAbsent(tenantId) {
            log.info("Creating R2DBC pool for tenant {}", tenantId)
            CachedDbResource(createPool("$baseDatabase-$tenantId"))
        }

        cached.touch()
        return cached.resource
    }

    private fun getGlobalPool(): ConnectionPool {

        val existing = globalPool
        if (existing != null) return existing

        synchronized(this) {
            val again = globalPool
            if (again != null) return again

            log.info("Creating GLOBAL R2DBC pool")

            val created = createPool(baseDatabase)
            globalPool = created
            return created
        }
    }

    private fun createPool(database: String): ConnectionPool {

        val ds = properties.dataSource

        val driver =
            when (ds.type) {
                DataSourceType.POSTGRES -> "postgresql"
                DataSourceType.MYSQL -> "mysql"
                else -> error("Invalid R2DBC type for SQL")
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
        val ttl = 30 * 60 * 1000L

        pools.entries.removeIf { (tenantId, cached) ->
            if (cached.isExpired(ttl)) {
                log.debug("Cleaning idle pool tenant={}", tenantId)
                cached.resource.dispose()
                true
            } else false
        }
    }

    @PreDestroy
    fun shutdown() {
        log.info("Shutting down TenantAwareConnectionFactory")
        pools.forEach { (id, cached) ->
            log.info("Closing pool tenant={}", id)
            cached.resource.dispose()
        }
        pools.clear()
        globalPool?.dispose()
    }
}
