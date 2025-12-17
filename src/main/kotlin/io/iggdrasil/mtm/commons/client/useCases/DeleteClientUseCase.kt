package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import models.ID

class DeleteClientUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(clientId: ID) {
        repository.deleteById(clientId)
    }
}