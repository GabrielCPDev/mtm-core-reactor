package io.iggdrasil.mtm.commons.notification

import io.iggdrasil.mtm.commons.tenantEvent.EventType
import models.AbstractEntity
import models.ID
import java.time.Instant

data class Subscriber private constructor(
    override val id: ID,
    val clientId: ID,
    val webhookUrl: WebhookUrl,
    val events: Set<EventType>,
    val enabled: Boolean,
    override val createdAt: Instant,
    override val updatedAt: Instant
) : AbstractEntity<ID>(id, createdAt, updatedAt) {

    fun isActive(): Boolean = enabled

    fun disable(): Subscriber =
        copy(enabled = false, updatedAt = Instant.now())

    fun enable(): Subscriber =
        copy(enabled = true, updatedAt = Instant.now())

    fun updateEvents(events: Set<EventType>): Subscriber =
        copy(events = events, updatedAt = Instant.now())

    fun isSubscribedTo(eventType: EventType): Boolean =
        enabled && events.contains(eventType)

    companion object {

        fun create(
            clientId: ID,
            webhookUrl: WebhookUrl,
            events: Set<EventType>
        ): Subscriber = Subscriber(
            id = ID.generate(),
            clientId = clientId,
            webhookUrl = webhookUrl,
            events = events,
            enabled = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        fun reconstitute(
            id: ID,
            clientId: ID,
            webhookUrl: WebhookUrl,
            events: Set<EventType>,
            enabled: Boolean,
            createdAt: Instant,
            updatedAt: Instant
        ): Subscriber = Subscriber(
            id = id,
            clientId = clientId,
            webhookUrl = webhookUrl,
            events = events,
            enabled = enabled,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
