package io.iggdrasil.mtm.db.metrics

data class MtMResourceSnapshot(
    val resourceType: String,
    val activeTenants: Int,
    val details: List<TenantLeaseInfo>
)