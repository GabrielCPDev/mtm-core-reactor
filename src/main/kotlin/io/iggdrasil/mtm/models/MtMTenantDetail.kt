package io.iggdrasil.mtm.models

import java.time.Instant

data class MtMTenantDetail(
    val tenantId: String,
    val idleMs: Long,
    val lastAccess: Instant
)