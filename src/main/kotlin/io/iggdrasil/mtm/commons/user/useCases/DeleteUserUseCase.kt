package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import models.ID
import utils.ResourceNotFoundException

class DeleteUserUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(id: ID) {
        val user = repository.findById(id)
            ?: throw ResourceNotFoundException("User with id $id not found")

        repository.deleteById(user.id)
    }
}