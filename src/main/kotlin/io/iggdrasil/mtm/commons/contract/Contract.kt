package io.iggdrasil.mtm.commons.contract

import models.AbstractEntity
import models.ID
import java.time.Instant

data class Contract(
    override val id: ID,
    val clientId: ID,
    val file: ContractFile? = null,
    val extraInstances: Int = 0,
    val active: Boolean = true,
    val expiresAt: Instant?,
    val uploadedAt: Instant? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : AbstractEntity<ID>(id, createdAt, updatedAt) {

    fun isExpired(): Boolean =
        expiresAt?.isBefore(Instant.now()) ?: false

    fun isActive(): Boolean =
        active && !isExpired()

    fun deactivate(): Contract =
        copy(active = false, updatedAt = Instant.now())

    companion object {

        fun create(
            clientId: ID,
            extraInstances: Int = 0,
            expiresAt: Instant? = null
        ): Contract = Contract(
            id = ID.generate(),
            clientId = clientId,
            file = null,
            extraInstances = extraInstances,
            active = true,
            expiresAt = expiresAt,
            uploadedAt = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
