package io.iggdrasil.mtm.db.providers.metrics

data class ProviderStatusInfo(
    val type: String,
    val activeResources: Int,
    val tenants: Set<String>,
    val maxPoolSize: Int?,
    val database: String?,
    val strategy: String
)
