package io.iggdrasil.mtm.db.providers.mysql

import io.iggdrasil.mtm.commons.tenant.TenancyDBStrategy
import io.iggdrasil.mtm.commons.tenant.Tenant
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ValidationDepth
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class MysqlR2dbcProvider(
    private val properties: MultiTenancyProperties
) : ConnectionProvider<ConnectionFactory> {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val pools = ConcurrentHashMap<String, ConnectionPool>()

    @Volatile
    private var globalPool: ConnectionPool? = null

    override fun createConnection(tenant: Tenant): ConnectionFactory {
        return pools.getOrPut(tenant.id.value.toString()) {
            logger.info("Creating MySQL R2DBC connection pool for tenant ${tenant.id}")

            val options = when (tenant.strategy) {
                TenancyDBStrategy.DATABASE ->
                    ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "mysql")
                        .option(ConnectionFactoryOptions.HOST, properties.dataSource.host)
                        .option(ConnectionFactoryOptions.PORT, properties.dataSource.port)
                        .option(ConnectionFactoryOptions.DATABASE, "tenant_${tenant.id.value}")
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
                    throw IllegalArgumentException("COLLECTION strategy not supported for MySQL")
            }

            val factory = ConnectionFactories.get(options)

            val poolConfig = ConnectionPoolConfiguration.builder(factory)
                .maxSize(properties.dataSource.maxPoolSize)
                .initialSize(2)
                .maxIdleTime(Duration.ofMinutes(30))
                .build()

            ConnectionPool(poolConfig)
        }
    }

    override suspend fun validateConnection(tenant: Tenant): Boolean {
        return try {
            val factory = createConnection(tenant)

            Mono.from(factory.create())
                .flatMap { connection ->
                    Mono.from(connection.validate(ValidationDepth.LOCAL))
                        .doFinally { Mono.from(connection.close()).subscribe() }
                }
                .awaitSingle()

            true
        } catch (e: Exception) {
            logger.error("Failed to validate MySQL connection for tenant ${tenant.id}", e)
            false
        }
    }

    override fun closeConnection(tenant: Tenant) {
        pools.remove(tenant.id.value.toString())?.let { pool ->
            logger.info("Closing MySQL R2DBC pool for tenant ${tenant.id}")
            pool.dispose()
        }
    }

    override fun createGlobalConnection(): ConnectionFactory {
        val existing = globalPool
        if (existing != null) return existing

        synchronized(this) {
            val again = globalPool
            if (again != null) return again

            logger.info("Creating GLOBAL MySQL R2DBC connection pool")

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
            logger.info("Closing GLOBAL MySQL R2DBC pool")
            connection.dispose()
        }
        globalPool = null
    }
}
