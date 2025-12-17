package io.iggdrasil.mtm.commons.client

import io.iggdrasil.mtm.commons.contract.Contract
import models.AbstractEntity
import models.ID
import models.Name
import java.time.Instant

data class Client(override val id: ID,
                  val userId: ID,
                  val name: Name,
                  val apiKey: ApiKey,
                  val activeInstances: Int = 0,
                  val contracts: List<Contract> = emptyList(),
                  val enabled: Boolean = true,
                  val expiresAt: Instant? = null,
                  override val createdAt: Instant = Instant.now(),
                  override val updatedAt: Instant = Instant.now()
) : AbstractEntity<ID>(id, createdAt, updatedAt) {

    fun isExpired(): Boolean =
        expiresAt?.isBefore(Instant.now()) ?: false

    fun isActive(): Boolean = enabled && !isExpired()

    fun disable(): Client =
        copy(enabled = false, updatedAt = Instant.now())

    val activeContract: Contract?
        get() = contracts
            .asSequence()
            .filter { it.isActive() }
            .maxWithOrNull(
                compareBy<Contract> { it.uploadedAt == null }
                    .thenBy { it.uploadedAt }
            )

    fun calculateMaxInstances(userPlanMaxInstances: Int): Int {
        return userPlanMaxInstances + (activeContract?.extraInstances ?: 0)
    }

    fun canStartInstance(userPlanMaxInstances: Int): Boolean {
        val maxAllowed = calculateMaxInstances(userPlanMaxInstances)
        return activeInstances < maxAllowed
    }

    fun incrementInstance(): Client =
        copy(activeInstances = activeInstances + 1, updatedAt = Instant.now())

    fun decrementInstance(): Client =
        copy(activeInstances = maxOf(0, activeInstances - 1), updatedAt = Instant.now())

    fun addContract(contract: Contract): Client {
        val updatedContracts = contracts.map { it.deactivate() }
        return copy(
            contracts = updatedContracts + contract,
            updatedAt = Instant.now()
        )
    }

    companion object {

        fun create(
            userId: ID,
            name: Name,
            expiresAt: Instant? = null
        ): Client = Client(
            id = ID.generate(),
            userId = userId,
            name = name,
            apiKey = ApiKey.Companion.generate(),
            activeInstances = 0,
            contracts = emptyList(),
            expiresAt = expiresAt,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        fun reconstitute(
            id: ID,
            userId: ID,
            name: Name,
            apiKey: ApiKey,
            activeInstances: Int,
            contracts: List<Contract>,
            enabled: Boolean,
            expiresAt: Instant?,
            createdAt: Instant,
            updatedAt: Instant
        ): Client = Client(
            id,
            userId,
            name,
            apiKey,
            activeInstances,
            contracts,
            enabled,
            expiresAt,
            createdAt,
            updatedAt
        )
    }
}
