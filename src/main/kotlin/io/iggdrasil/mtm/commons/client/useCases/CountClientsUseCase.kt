package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort

class CountClientsUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(): Long {
        return repository.count()
    }
}