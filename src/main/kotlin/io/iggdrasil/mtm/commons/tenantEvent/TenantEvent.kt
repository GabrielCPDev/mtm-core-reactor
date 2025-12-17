package io.iggdrasil.mtm.commons.tenantEvent

import models.AbstractEntity
import models.ID
import java.time.Instant
import io.iggdrasil.mtm.commons.tenant.Metadata

class TenantEvent private constructor(
    override val id: ID,
    val tenantId: ID,
    val clientId: ID,
    val type: EventType,
    val occurredAt: Instant,
    val metadata: Metadata,
    override val createdAt: Instant,
    override val updatedAt: Instant
) : AbstractEntity<ID>(id, createdAt, updatedAt) {

    companion object {

        fun create(
            tenantId: ID,
            clientId: ID,
            type: EventType,
            metadata: Metadata = Metadata.empty()
        ): TenantEvent {
            val now = Instant.now()

            return TenantEvent(
                id = ID.generate(),
                tenantId = tenantId,
                clientId = clientId,
                type = type,
                occurredAt = now,
                metadata = metadata,
                createdAt = now,
                updatedAt = now
            )
        }

        fun reconstitute(
            id: ID,
            tenantId: ID,
            clientId: ID,
            type: EventType,
            occurredAt: Instant,
            metadata: Metadata,
            createdAt: Instant,
            updatedAt: Instant
        ): TenantEvent = TenantEvent(
            id = id,
            tenantId = tenantId,
            clientId = clientId,
            type = type,
            occurredAt = occurredAt,
            metadata = metadata,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
