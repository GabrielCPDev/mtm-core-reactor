package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import models.ID
import utils.ResourceNotFoundException

class GetContractUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(contractId: ID): Contract {
        return repository.findById(contractId) ?: throw ResourceNotFoundException("Contract with id $contractId does not exists")
    }
}