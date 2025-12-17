package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import models.ID

class DeleteContractUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(contractId: ID) {
        repository.deleteById(contractId)
    }
}