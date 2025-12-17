package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import io.iggdrasil.mtm.commons.user.User
import utils.DuplicateResourceException

class CreateUserUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(user: User): User {
        if (repository.existsByEmail(user.email))
            throw DuplicateResourceException("User with email ${user.email} already exists")

        return repository.save(user)
    }
}