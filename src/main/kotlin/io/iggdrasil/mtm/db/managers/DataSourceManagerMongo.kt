package io.iggdrasil.mtm.db.managers

import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.config.providers.ConnectionProvider

class DataSourceManagerMongo(
    private val connectionProvider: ConnectionProvider<MongoDatabase>
) {

    @Volatile
    private var globalDatabase: MongoDatabase? = null

    fun getDatabaseForTenant(tenantId: String?): MongoDatabase {
        return if (tenantId == null)
            getGlobalMongoDatabase()
        else
            connectionProvider.createConnection(tenantId)
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
}
