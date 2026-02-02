package io.iggdrasil.mtm.config.providers

interface ConnectionProvider<T> {

    fun createConnection(tenantId: String): T

    suspend fun validateConnection(tenantId: String): Boolean

    fun closeConnection(tenantId: String)

    fun createGlobalConnection(): T

    fun closeGlobalConnection(connection: T)
}
