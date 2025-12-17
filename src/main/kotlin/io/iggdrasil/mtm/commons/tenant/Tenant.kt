package io.iggdrasil.mtm.commons.tenant

import models.AbstractEntity
import models.ID
import java.time.Instant

@ConsistentCopyVisibility
data class Tenant private constructor(
    override val id: ID,
    val clientId: ID,
    val enabled: Boolean,
    val expiresAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val dataSource: DataSourceType = DataSourceType.POSTGRES,
    val strategy: TenancyDBStrategy = TenancyDBStrategy.SCHEMA
) : AbstractEntity<ID>(id, createdAt, updatedAt) {

    fun isExpired(): Boolean =
        expiresAt?.isBefore(Instant.now()) ?: false

    fun isActive(): Boolean =
        enabled && !isExpired()

    fun disable(): Tenant =
        copy(enabled = false, updatedAt = Instant.now())

    companion object {

        fun create(
            clientId: ID,
            dataSource: DataSourceType = DataSourceType.POSTGRES,
            strategy: TenancyDBStrategy = TenancyDBStrategy.SCHEMA,
            expiresAt: Instant? = null
        ): Tenant = Tenant(
            id = ID.generate(),
            clientId = clientId,
            enabled = true,
            expiresAt = expiresAt,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            dataSource = dataSource,
            strategy = strategy
        )

        fun reconstitute(
            id: ID,
            clientId: ID,
            enabled: Boolean,
            expiresAt: Instant?,
            createdAt: Instant,
            updatedAt: Instant,
            dataSource: DataSourceType,
            strategy: TenancyDBStrategy
        ): Tenant = Tenant(
            id = id,
            clientId = clientId,
            enabled = enabled,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            dataSource = dataSource,
            strategy = strategy
        )
    }
}
