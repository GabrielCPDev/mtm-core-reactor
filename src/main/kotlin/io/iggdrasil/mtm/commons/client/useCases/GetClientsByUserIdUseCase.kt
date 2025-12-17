package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import kotlinx.coroutines.flow.Flow
import models.ID

class GetClientsByUserIdUseCase(
    private val repository: ClientRepositoryPort
) {
    fun execute(userId: ID): Flow<Client> {
        return repository.findByUserId(userId)
    }
}