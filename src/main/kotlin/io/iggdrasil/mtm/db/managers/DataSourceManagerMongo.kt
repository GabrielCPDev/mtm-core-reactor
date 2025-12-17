package io.iggdrasil.mtm.db.managers

import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.commons.tenant.Tenant
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.config.providers.TenantProvider
import io.iggdrasil.mtm.tenant.TenantContextHolder
import kotlinx.coroutines.reactor.awaitSingleOrNull
import java.util.concurrent.ConcurrentHashMap

class DataSourceManagerMongo(
    private val tenantContext: TenantContextHolder,
    private val tenantProvider: TenantProvider,
    private val connectionProvider: ConnectionProvider<MongoDatabase>
) {

    private val tenantCache = ConcurrentHashMap<String, Tenant>()
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

    private suspend fun createConnection(tenantId: String): MongoDatabase {
        val tenant = tenantProvider.getTenantById(tenantId)
            ?: throw IllegalStateException("Tenant $tenantId not found")

        tenantCache[tenantId] = tenant

        return connectionProvider.createConnection(tenant)
    }
}
