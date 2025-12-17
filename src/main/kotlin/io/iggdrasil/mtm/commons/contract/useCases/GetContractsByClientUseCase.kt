package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import kotlinx.coroutines.flow.Flow
import models.ID

class GetContractsByClientUseCase(
    private val repository: ContractRepositoryPort
) {
    fun execute(clientId: ID, page: Int = 0, size: Int = 20): Flow<Contract> {
        return repository.findByClientId(clientId, page, size)
    }
}