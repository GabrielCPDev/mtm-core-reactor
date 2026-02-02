package io.iggdrasil.mtm.db.managers

import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.r2dbc.spi.ConnectionFactory

class DataSourceManagerR2dbc(
    private val connectionProvider: ConnectionProvider<ConnectionFactory>
) {

    @Volatile
    private var globalConnection: ConnectionFactory? = null

    fun getFactoryForTenant(tenantId: String?): ConnectionFactory {
        return if (tenantId == null)
            getGlobalR2dbcFactory()
        else
            connectionProvider.createConnection(tenantId)
    }

    fun getGlobalR2dbcFactory(): ConnectionFactory {
        val existing = globalConnection
        if (existing != null) return existing

        synchronized(this) {
            val again = globalConnection
            if (again != null) return again

            val created = connectionProvider.createGlobalConnection()
            globalConnection = created
            return created
        }
    }
}
