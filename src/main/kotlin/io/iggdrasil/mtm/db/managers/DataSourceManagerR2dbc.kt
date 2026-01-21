package io.iggdrasil.mtm.db.managers

import io.iggdrasil.mtm.commons.tenant.TenancyDBStrategy
import io.iggdrasil.mtm.commons.tenant.Tenant
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.tenant.TenantContextHolder
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactor.awaitSingleOrNull
import models.ID
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class DataSourceManagerR2dbc(
    private val tenantContext: TenantContextHolder,
    private val properties: MultiTenancyProperties,
    private val connectionProvider: ConnectionProvider<ConnectionFactory>
) {

    private val connectionCache = ConcurrentHashMap<String, ConnectionFactory>()

    @Volatile
    private var globalConnection: ConnectionFactory? = null

    private suspend fun resolveTenantId(): String? =
        tenantContext.getReactive().awaitSingleOrNull()

    suspend fun getCurrentR2dbcFactory(): ConnectionFactory {
        val tenantId = resolveTenantId()
        return if (tenantId == null) {
            getGlobalR2dbcFactory()
        } else {
            getR2dbcFactoryForTenant(tenantId)
        }
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

    private suspend fun getR2dbcFactoryForTenant(tenantId: String): ConnectionFactory {
        return connectionCache.getOrPut(tenantId) {
            createConnection(tenantId)
        }
    }

    private fun createConnection(tenantId: String): ConnectionFactory {
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