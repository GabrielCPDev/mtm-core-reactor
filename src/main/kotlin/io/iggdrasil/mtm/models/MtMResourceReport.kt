package io.iggdrasil.mtm.models

data class MtMResourceReport(
    val type: String,
    val activeTenants: Int,
    val details: List<MtMTenantDetail>
)