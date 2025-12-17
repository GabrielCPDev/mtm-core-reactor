package io.iggdrasil.mtm.commons.client.status.useCases

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.client.status.ClientStatus
import io.iggdrasil.mtm.commons.client.status.ClientStatusEvent
import io.iggdrasil.mtm.commons.client.status.ClientStatusResult
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import models.ID
import utils.ForbiddenException
import utils.ResourceNotFoundException

class HandleClientStatusUseCase(
    private val clientRepository: ClientRepositoryPort,
    private val userRepository: UserRepositoryPort
) {
    suspend fun execute(clientId: ID,command: ClientStatus): ClientStatusResult {
        val client = clientRepository.findById(clientId)
            ?: throw ResourceNotFoundException("Client not found")

        return when (command.event) {
            ClientStatusEvent.STARTING -> handleStarting(client)
            ClientStatusEvent.STOPPING -> handleStopping(client)
        }
    }

    private suspend fun handleStarting(client: Client): ClientStatusResult {
        if (!client.isActive()) {
            throw ForbiddenException("Client is not active")
        }

        val user = userRepository.findById(client.userId)
            ?: throw IllegalStateException("User not found")

        if (!user.enabled) {
            throw ForbiddenException("User is disabled")
        }

        val maxInstances = client.calculateMaxInstances(user.plan.maxInstancesPerClient)

        if (!client.canStartInstance(user.plan.maxInstancesPerClient)) {
            return ClientStatusResult(
                allowed = false,
                currentInstances = client.activeInstances,
                maxInstances = maxInstances,
                message = "Instance limit reached"
            )
        }

        val updatedClient = client.incrementInstance()
        clientRepository.save(updatedClient)

        return ClientStatusResult(
            allowed = true,
            currentInstances = updatedClient.activeInstances,
            maxInstances = maxInstances,
            message = "Instance started"
        )
    }

    private suspend fun handleStopping(client: Client): ClientStatusResult {
        val user = userRepository.findById(client.userId)
            ?: throw IllegalStateException("User not found")

        val maxInstances = client.calculateMaxInstances(user.plan.maxInstancesPerClient)

        val updatedClient = client.decrementInstance()
        clientRepository.save(updatedClient)

        return ClientStatusResult(
            allowed = true,
            currentInstances = updatedClient.activeInstances,
            maxInstances = maxInstances,
            message = "Instance stopped"
        )
    }
}