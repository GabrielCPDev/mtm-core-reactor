package io.iggdrasil.mtm.commons.client.status

data class ClientStatusResult(
    val allowed: Boolean,
    val currentInstances: Int,
    val maxInstances: Int,
    val message: String
)