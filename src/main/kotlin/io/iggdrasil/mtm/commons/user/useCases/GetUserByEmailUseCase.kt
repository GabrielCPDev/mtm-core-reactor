package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import io.iggdrasil.mtm.commons.user.User
import models.Email
import utils.ResourceNotFoundException

class GetUserByEmailUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(id: Email): User {
        return repository.findByEmail(id)
            ?: throw ResourceNotFoundException("User with email $id not found")
    }
}