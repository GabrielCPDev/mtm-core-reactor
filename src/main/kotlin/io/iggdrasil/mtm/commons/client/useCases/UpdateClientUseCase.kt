package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort

class UpdateClientUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(client: Client): Client {
        return repository.update(client)
    }
}