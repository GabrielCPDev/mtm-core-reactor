package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.ApiKey
import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import utils.ResourceNotFoundException

class GetClientByApiKeyUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(apiKey: ApiKey): Client {
        return repository.findByApiKey(apiKey)
            ?: throw ResourceNotFoundException("Client with api key: $apiKey does not exists")
    }
}