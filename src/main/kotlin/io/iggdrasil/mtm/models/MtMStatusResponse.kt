package io.iggdrasil.mtm.models

import java.time.Instant

data class MtMStatusResponse(
    val engine: String,
    val timestamp: Instant = Instant.now(),
    val reports: List<MtMResourceReport>
)