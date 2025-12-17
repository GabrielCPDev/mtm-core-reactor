package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import io.iggdrasil.mtm.commons.user.User
import models.ID
import utils.ResourceNotFoundException
import java.time.Instant

class UpdateUserUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(id: ID, updated: User): User {
        val existing = repository.findById(id)
            ?: throw ResourceNotFoundException("User with id $id not found")

        val merged = existing.copy(
            name = updated.name ?: existing.name,
            email = updated.email ?: existing.email,
            password = updated.password ?: existing.password,
            role = updated.role ?: existing.role,
            enabled = updated.enabled,
            updatedAt = Instant.now()
        )

        return repository.update(merged)
    }
}