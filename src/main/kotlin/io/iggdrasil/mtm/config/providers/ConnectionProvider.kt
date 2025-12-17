package io.iggdrasil.mtm.config.providers

import io.iggdrasil.mtm.commons.tenant.Tenant

interface ConnectionProvider<T> {

    fun createConnection(tenant: Tenant): T

    suspend fun validateConnection(tenant: Tenant): Boolean

    fun closeConnection(tenant: Tenant)

    fun createGlobalConnection(): T

    fun closeGlobalConnection(connection: T)
}
