package io.iggdrasil.mtm.db.metrics

import java.time.Instant

data class TenantLeaseInfo(
    val tenantId: String,
    val lastActive: Instant,
    val status: String
)