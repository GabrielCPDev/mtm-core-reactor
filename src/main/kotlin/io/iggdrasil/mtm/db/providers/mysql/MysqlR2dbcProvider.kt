package io.iggdrasil.mtm.db.providers.mysql

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.db.providers.metrics.ProviderStatus
import io.iggdrasil.mtm.db.providers.metrics.ProviderStatusInfo
import io.iggdrasil.mtm.tenant.TenancyDBStrategy
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ValidationDepth
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class MysqlR2dbcProvider(
    private val properties: MultiTenancyProperties
) : ConnectionProvider<ConnectionFactory>, ProviderStatus {

    private val logger = LoggerFactory.getLogger(javaClass)

    private data class CachedPool(
        val pool: ConnectionPool,
        @Volatile var lastAccess: Long = System.currentTimeMillis()
    )

    private val pools = ConcurrentHashMap<String, CachedPool>()

    @Volatile
    private var globalPool: ConnectionPool? = null

    override fun createConnection(tenantId: String): ConnectionFactory {

        val cached = pools.computeIfAbsent(tenantId) {

            logger.info("Creating MySQL R2DBC pool for tenant {}", tenantId)

            val options = when (properties.strategy) {

                TenancyDBStrategy.DATABASE ->
                    ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "mysql")
                        .option(ConnectionFactoryOptions.HOST, properties.dataSource.host)
                        .option(ConnectionFactoryOptions.PORT, properties.dataSource.port)
                        .option(ConnectionFactoryOptions.DATABASE, "tenant_$tenantId")
                        .option(ConnectionFactoryOptions.USER, properties.dataSource.username)
                        .option(ConnectionFactoryOptions.PASSWORD, properties.dataSource.password)
                        .build()

                TenancyDBStrategy.SCHEMA ->
                    ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "mysql")
                        .option(ConnectionFactoryOptions.HOST, properties.dataSource.host)
                        .option(ConnectionFactoryOptions.PORT, properties.dataSource.port)
                        .option(ConnectionFactoryOptions.DATABASE, properties.dataSource.database)
                        .option(ConnectionFactoryOptions.USER, properties.dataSource.username)
                        .option(ConnectionFactoryOptions.PASSWORD, properties.dataSource.password)
                        .build()

                TenancyDBStrategy.COLLECTION ->
                    throw IllegalArgumentException(
                        "COLLECTION strategy not supported for MySQL"
                    )
            }

            val factory = ConnectionFactories.get(options)

            val poolConfig = ConnectionPoolConfiguration.builder(factory)
                .maxSize(properties.dataSource.maxPoolSize)
                .initialSize(2)
                .maxIdleTime(Duration.ofMinutes(30))
                .build()

            CachedPool(ConnectionPool(poolConfig))
        }

        cached.lastAccess = System.currentTimeMillis()
        return cached.pool
    }

    override suspend fun validateConnection(tenantId: String): Boolean {
        return try {
            val factory = createConnection(tenantId)

            Mono.from(factory.create())
                .flatMap { connection ->
                    Mono.from(connection.validate(ValidationDepth.LOCAL))
                        .flatMap {
                            Mono.from(connection.close()).thenReturn(true)
                        }
                        .onErrorResume {
                            Mono.from(connection.close()).thenReturn(false)
                        }
                }
                .awaitSingle()

        } catch (e: Exception) {
            logger.error(
                "Failed to validate MySQL connection for tenant {}",
                tenantId,
                e
            )
            false
        }
    }

    override fun closeConnection(tenantId: String) {
        pools.remove(tenantId)?.let { cached ->
            logger.info("Closing MySQL pool for tenant {}", tenantId)
            cached.pool.dispose()
        }
    }

    override fun createGlobalConnection(): ConnectionFactory {
        val existing = globalPool
        if (existing != null) return existing

        synchronized(this) {
            val again = globalPool
            if (again != null) return again

            logger.info("Creating GLOBAL MySQL pool")

            val options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "mysql")
                .option(ConnectionFactoryOptions.HOST, properties.dataSource.host)
                .option(ConnectionFactoryOptions.PORT, properties.dataSource.port)
                .option(ConnectionFactoryOptions.DATABASE, properties.dataSource.database)
                .option(ConnectionFactoryOptions.USER, properties.dataSource.username)
                .option(ConnectionFactoryOptions.PASSWORD, properties.dataSource.password)
                .build()

            val factory = ConnectionFactories.get(options)

            val poolConfig = ConnectionPoolConfiguration.builder(factory)
                .maxSize(properties.dataSource.maxPoolSize)
                .initialSize(2)
                .maxIdleTime(Duration.ofMinutes(30))
                .build()

            val pool = ConnectionPool(poolConfig)
            globalPool = pool
            return pool
        }
    }

    override fun closeGlobalConnection(connection: ConnectionFactory) {
        if (connection is ConnectionPool) {
            logger.info("Closing GLOBAL MySQL pool")
            connection.dispose()
        }
        globalPool = null
    }

    /**
     * Remove pools sem uso por 30 minutos
     */
    @Scheduled(fixedDelay = 300000)
    fun cleanupPools() {

        val now = System.currentTimeMillis()
        val ttl = 30 * 60 * 1000L

        pools.entries.removeIf { (tenantId, cached) ->

            val expired = now - cached.lastAccess > ttl

            if (expired) {
                logger.debug(
                    "Cleaning idle MySQL pool tenant={}",
                    tenantId
                )
                cached.pool.dispose()
            }

            expired
        }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down MySQL pools")

        pools.forEach { (tenantId, pool) ->
            try {
                logger.info("Closing MySQL pool for tenant {}", tenantId)
                pool.pool.dispose()
            } catch (e: Exception) {
                logger.warn("Error closing MySQL pool for tenant {}", tenantId, e)
            }
        }

        pools.clear()

        try {
            globalPool?.let {
                logger.info("Closing GLOBAL MySQL pool")
                it.dispose()
            }
        } catch (e: Exception) {
            logger.warn("Error closing GLOBAL MySQL pool", e)
        } finally {
            globalPool = null
        }
    }

    override fun status(): ProviderStatusInfo =
        ProviderStatusInfo(
            type = "MYSQL",
            activeResources = pools.size,
            tenants = pools.keys.toSet(),
            maxPoolSize = properties.dataSource.maxPoolSize,
            database = properties.dataSource.database,
            strategy = properties.strategy.name
        )
}
