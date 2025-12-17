package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import models.ID

class CheckClientExistsByNameUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(name: String, userId: ID): Boolean {
        return repository.existsByNameAndUserId(name, userId)
    }
}