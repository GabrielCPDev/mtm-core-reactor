package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort

class DeleteAllUsersUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute() {
        repository.deleteAll()
    }
}