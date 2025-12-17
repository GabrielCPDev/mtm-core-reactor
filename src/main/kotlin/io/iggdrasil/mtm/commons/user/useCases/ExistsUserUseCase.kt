package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import models.ID

class ExistsUserUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(id: ID): Boolean {
        return repository.exists(id)
    }
}