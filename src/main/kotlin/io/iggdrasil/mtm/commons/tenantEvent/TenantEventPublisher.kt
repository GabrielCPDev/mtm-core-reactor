package io.iggdrasil.mtm.commons.tenantEvent

interface TenantEventPublisher {
    suspend fun publish(event: TenantEvent)
}