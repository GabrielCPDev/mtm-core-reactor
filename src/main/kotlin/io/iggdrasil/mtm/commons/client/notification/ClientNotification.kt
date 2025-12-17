package io.iggdrasil.mtm.commons.client.notification

import java.time.Instant

data class ClientNotification(
    val clientId: String,
    val event: ClientEvent,
    val reason: String,
    val timestamp: Instant = Instant.now()
)
