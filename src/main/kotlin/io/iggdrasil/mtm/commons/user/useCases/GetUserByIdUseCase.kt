package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import io.iggdrasil.mtm.commons.user.User
import models.ID
import utils.ResourceNotFoundException

class GetUserByIdUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(id: ID): User {
        return repository.findById(id)
            ?: throw ResourceNotFoundException("User with id $id not found")
    }
}