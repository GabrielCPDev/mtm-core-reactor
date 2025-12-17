package io.iggdrasil.mtm.db.providers.mongo

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.commons.tenant.TenancyDBStrategy
import io.iggdrasil.mtm.commons.tenant.Tenant
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class MongoReactiveProvider(
    private val properties: MultiTenancyProperties
) : ConnectionProvider<MongoDatabase> {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val clients = ConcurrentHashMap<String, MongoClient>()
    private val databases = ConcurrentHashMap<String, MongoDatabase>()

    @Volatile
    private var globalClient: MongoClient? = null

    @Volatile
    private var globalDatabase: MongoDatabase? = null

    override fun createConnection(tenant: Tenant): MongoDatabase {
        val tenantKey = tenant.id.value.toString()

        return databases.getOrPut(tenantKey) {
            logger.info("Creating MongoDB Reactive connection for tenant $tenantKey")

            val client = clients.getOrPut(tenantKey) {
                val connectionString = buildConnectionString()
                MongoClients.create(ConnectionString(connectionString))
            }

            val databaseName = when (tenant.strategy) {
                TenancyDBStrategy.DATABASE ->
                    "tenant_${tenant.id.value}"

                TenancyDBStrategy.COLLECTION ->
                    properties.dataSource.database

                TenancyDBStrategy.SCHEMA ->
                    throw IllegalArgumentException("SCHEMA strategy is not supported for MongoDB")
            }

            client.getDatabase(databaseName)
        }
    }

    override suspend fun validateConnection(tenant: Tenant): Boolean {
        return try {
            val db = createConnection(tenant)
            db.listCollectionNames().awaitFirstOrNull()
            true
        } catch (e: Exception) {
            logger.error("Failed to validate MongoDB connection for tenant ${tenant.id}", e)
            false
        }
    }

    override fun closeConnection(tenant: Tenant) {
        val tenantKey = tenant.id.value.toString()

        databases.remove(tenantKey)
        clients.remove(tenantKey)?.let { client ->
            logger.info("Closing MongoDB client for tenant $tenantKey")
            client.close()
        }
    }

    override fun createGlobalConnection(): MongoDatabase {
        val existing = globalDatabase
        if (existing != null) return existing

        synchronized(this) {
            val again = globalDatabase
            if (again != null) return again

            logger.info("Creating GLOBAL MongoDB connection")

            val client = MongoClients.create(ConnectionString(buildConnectionString()))
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
}
