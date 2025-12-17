package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import kotlinx.coroutines.flow.toList
import models.ID
import models.Name
import utils.DuplicateResourceException
import utils.ForbiddenException
import utils.LimitExceededException
import utils.ResourceNotFoundException

import java.time.Instant

class CreateClientUseCase(
    private val clientRepository: ClientRepositoryPort,
    private val userRepository: UserRepositoryPort
) {
    suspend fun execute(
        userId: ID,
        name: Name,
        expiresAt: Instant? = null
    ): Client {
        val user = userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found")

        if (!user.enabled) {
            throw ForbiddenException("User is disabled")
        }

        val currentClient = clientRepository.findByUserId(userId)
            .toList()
        user.copy(clients = currentClient)

        if (!user.canCreateClient()) {
            throw LimitExceededException(
                "Client limit reached. Current: ${currentClient.size}, Max: ${user.plan.maxClients}"
            )
        }

        if (clientRepository.existsByNameAndUserId(name.value, userId)) {
            throw DuplicateResourceException("Client with name '${name.value}' already exists")
        }

        val client = Client.create(
            userId = userId,
            name = name,
            expiresAt = expiresAt
        )

        return clientRepository.save(client)
    }
}