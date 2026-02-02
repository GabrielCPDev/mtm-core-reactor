package io.iggdrasil.mtm.db.providers.mongo

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.db.providers.metrics.ProviderStatus
import io.iggdrasil.mtm.db.providers.metrics.ProviderStatusInfo
import io.iggdrasil.mtm.tenant.TenancyDBStrategy
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ConcurrentHashMap

class MongoReactiveProvider(
    private val properties: MultiTenancyProperties
) : ConnectionProvider<MongoDatabase>, ProviderStatus {

    private val logger = LoggerFactory.getLogger(javaClass)

    private data class CachedClient(
        val client: MongoClient,
        @Volatile var lastAccess: Long = System.currentTimeMillis()
    )

    private val clients = ConcurrentHashMap<String, CachedClient>()

    @Volatile
    private var globalClient: MongoClient? = null

    @Volatile
    private var globalDatabase: MongoDatabase? = null

    override fun createConnection(tenantId: String): MongoDatabase {

        val cached = clients.computeIfAbsent(tenantId) {

            logger.info("Creating MongoDB client for tenant {}", tenantId)

            CachedClient(
                MongoClients.create(ConnectionString(buildConnectionString()))
            )
        }

        cached.lastAccess = System.currentTimeMillis()

        val databaseName = when (properties.strategy) {

            TenancyDBStrategy.DATABASE ->
                "tenant_$tenantId"

            TenancyDBStrategy.COLLECTION ->
                properties.dataSource.database

            TenancyDBStrategy.SCHEMA ->
                throw IllegalArgumentException(
                    "SCHEMA strategy is not supported for MongoDB"
                )
        }

        return cached.client.getDatabase(databaseName)
    }

    override suspend fun validateConnection(tenantId: String): Boolean {
        return try {
            createConnection(tenantId)
                .listCollectionNames()
                .awaitFirstOrNull()
            true
        } catch (e: Exception) {
            logger.error(
                "Failed to validate MongoDB connection for tenant {}",
                tenantId,
                e
            )
            false
        }
    }

    override fun closeConnection(tenantId: String) {
        clients.remove(tenantId)?.let {
            logger.info("Closing MongoDB client for tenant {}", tenantId)
            it.client.close()
        }
    }

    override fun createGlobalConnection(): MongoDatabase {
        val existing = globalDatabase
        if (existing != null) return existing

        synchronized(this) {
            val again = globalDatabase
            if (again != null) return again

            logger.info("Creating GLOBAL MongoDB connection")

            val client =
                MongoClients.create(ConnectionString(buildConnectionString()))

            val db = client.getDatabase(properties.dataSource.database)

            globalClient = client
            globalDatabase = db
            return db
        }
    }

    override fun closeGlobalConnection(connection: MongoDatabase) {
        globalDatabase = null

        globalClient?.let {
            logger.info("Closing GLOBAL MongoDB client")
            it.close()
        }

        globalClient = null
    }

    @Scheduled(fixedDelay = 300000)
    fun cleanupClients() {

        val now = System.currentTimeMillis()
        val ttl = 30 * 60 * 1000L

        clients.entries.removeIf { (tenantId, cached) ->

            val expired = now - cached.lastAccess > ttl

            if (expired) {
                logger.debug(
                    "Cleaning idle MongoDB client tenant={}",
                    tenantId
                )
                cached.client.close()
            }

            expired
        }
    }


    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down MongoReactiveProvider")
        try {
            clients.forEach { (tenantId, cached) ->
                try {
                    logger.info("Closing MongoDB client for tenant {}", tenantId)
                    cached.client.close()
                } catch (e: Exception) {
                    logger.warn("Error closing MongoDB client for tenant {}", tenantId, e)
                }
            }
            clients.clear()

            globalClient?.let {
                logger.info("Closing GLOBAL MongoDB client")
                it.close()
            }
        } catch (e: Exception) {
            logger.warn("Error closing GLOBAL MongoDB client", e)
        } finally {
            globalClient = null
            globalDatabase = null
        }
    }

    private fun buildConnectionString(): String {
        val user = properties.dataSource.username
        val pass = properties.dataSource.password
        val host = properties.dataSource.host
        val port = properties.dataSource.port

        return if (user.isNotBlank() && pass.isNotBlank()) {
            "mongodb://$user:$pass@$host:$port"
        } else {
            "mongodb://$host:$port"
        }
    }

    override fun status(): ProviderStatusInfo =
        ProviderStatusInfo(
            type = "MONGO",
            activeResources = clients.size,
            tenants = clients.keys,
            maxPoolSize = null,
            database = properties.dataSource.database,
            strategy = properties.strategy.name
        )
}
