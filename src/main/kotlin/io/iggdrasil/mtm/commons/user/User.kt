package io.iggdrasil.mtm.commons.user

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.user.password.Password
import io.iggdrasil.mtm.commons.user.plan.PlanType
import models.AbstractEntity
import models.Email
import models.ID
import models.Name

import java.time.Instant

data class User(
    override val id: ID,
    val name: Name,
    val tenantId: ID,
    val email: Email,
    var password: Password,
    val role: UserRole,
    val plan: PlanType,
    val clients: List<Client> = emptyList(),
    var enabled: Boolean = true,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : AbstractEntity<ID>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt
) {

    fun changePassword(password: Password) = apply {
        this.password = password
    }

    fun disable() = apply {
        enabled = false
    }

    fun canCreateClient(): Boolean =
        clients.size + 1 < plan.maxClients

    fun canClientStartInstance(clientId: ID): Boolean {
        val client = clients.find { it.id == clientId } ?: return false
        val maxInstances = plan.maxInstancesPerClient + (client.activeContract?.extraInstances ?: 0)
        return client.activeInstances < maxInstances
    }

    fun addClient(client: Client): User {
        require(canCreateClient()) { "Client limit reached" }
        return copy(
            clients = clients + client,
            updatedAt = Instant.now()
        )
    }

    fun incrementClientInstance(clientId: ID): User {
        require(canClientStartInstance(clientId)) { "Instance limit reached" }

        val updatedClients = clients.map {
            if (it.id == clientId) it.incrementInstance() else it
        }

        return copy(
            clients = updatedClients,
            updatedAt = Instant.now()
        )
    }

    fun decrementClientInstance(clientId: ID): User {
        val updatedClients = clients.map {
            if (it.id == clientId) it.decrementInstance() else it
        }

        return copy(
            clients = updatedClients,
            updatedAt = Instant.now()
        )
    }

    companion object {

        fun create(
            name: Name,
            email: Email,
            tenantId: ID,
            password: Password,
            role: UserRole = UserRole.CLIENT,
            plan: PlanType = PlanType.FREE
        ): User {
            val now = Instant.now()
            return User(
                id = ID.generate(),
                name = name,
                tenantId = tenantId,
                email = email,
                password = password,
                role = role,
                plan = plan,
                clients = emptyList(),
                enabled = true,
                createdAt = now,
                updatedAt = now
            )
        }

        fun reconstitute(
            id: ID,
            tenantId: ID,
            name: Name,
            email: Email,
            password: Password,
            role: UserRole,
            plan: PlanType,
            clients: List<Client>,
            enabled: Boolean,
            createdAt: Instant,
            updatedAt: Instant
        ): User {
            return User(
                id = id,
                name = name,
                tenantId = tenantId,
                email = email,
                password = password,
                role = role,
                plan = plan,
                clients = clients,
                enabled = enabled,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}
