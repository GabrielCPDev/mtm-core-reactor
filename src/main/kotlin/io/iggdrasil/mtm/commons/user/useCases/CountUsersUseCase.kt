package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort

class CountUsersUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(): Long {
        return repository.count()
    }
}