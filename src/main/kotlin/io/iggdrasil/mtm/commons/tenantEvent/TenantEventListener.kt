package io.iggdrasil.mtm.commons.tenantEvent

interface TenantEventListener {
    suspend fun onEvent(event: TenantEvent)
}