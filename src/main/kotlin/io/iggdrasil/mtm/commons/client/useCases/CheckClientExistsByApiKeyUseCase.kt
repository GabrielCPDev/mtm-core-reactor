package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.ApiKey
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort

class CheckClientExistsByApiKeyUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(apiKey: ApiKey): Boolean {
        return repository.existsByApiKey(apiKey)
    }
}