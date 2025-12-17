package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort

class CountContractsUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(): Long {
        return repository.count()
    }
}