package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import models.Email

class ExistsUserByEmailUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(email: Email): Boolean {
        return repository.existsByEmail(email)
    }
}