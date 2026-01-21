package io.iggdrasil.mtm.db.managers

import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.commons.tenant.TenancyDBStrategy
import io.iggdrasil.mtm.commons.tenant.Tenant
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.tenant.TenantContextHolder
import kotlinx.coroutines.reactor.awaitSingleOrNull
import models.ID
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class DataSourceManagerMongo(
    private val tenantContext: TenantContextHolder,
    private val properties: MultiTenancyProperties,
    private val connectionProvider: ConnectionProvider<MongoDatabase>
) {

    private val databaseCache = ConcurrentHashMap<String, MongoDatabase>()

    @Volatile
    private var globalDatabase: MongoDatabase? = null

    private suspend fun resolveTenantId(): String? =
        tenantContext.getReactive().awaitSingleOrNull()

    suspend fun getCurrentMongoDatabase(): MongoDatabase {
        val tenantId = resolveTenantId()
        return if (tenantId == null) {
            getGlobalMongoDatabase()
        } else {
            getMongoDatabaseForTenant(tenantId)
        }
    }

    fun getGlobalMongoDatabase(): MongoDatabase {
        val existing = globalDatabase
        if (existing != null) return existing

        synchronized(this) {
            val again = globalDatabase
            if (again != null) return again

            val created = connectionProvider.createGlobalConnection()
            globalDatabase = created
            return created
        }
    }

    private suspend fun getMongoDatabaseForTenant(tenantId: String): MongoDatabase {
        return databaseCache.getOrPut(tenantId) {
            createConnection(tenantId)
        }
    }

    private fun createConnection(tenantId: String): MongoDatabase {
        val tenant = Tenant.reconstitute(
            id = ID.from(tenantId),
            clientId = ID.generate(),
            enabled = true,
            expiresAt = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            dataSource = properties.dataSource.type,
            strategy = TenancyDBStrategy.SCHEMA
        )

        return connectionProvider.createConnection(tenant)
    }
}