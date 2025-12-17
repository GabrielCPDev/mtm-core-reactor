package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort

class UpdateContractUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(contract: Contract): Contract {
        return repository.update(contract)
    }
}