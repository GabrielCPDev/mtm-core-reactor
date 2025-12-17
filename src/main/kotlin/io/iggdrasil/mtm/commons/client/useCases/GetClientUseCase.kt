package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import models.ID
import utils.ResourceNotFoundException

class GetClientUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(clientId: ID): Client {
        return repository.findById(clientId) ?: throw ResourceNotFoundException("Client with id $clientId does not exists")
    }
}