package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Sha256
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import models.ID

class CheckContractExistsUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(clientId: ID, checksum: Sha256): Boolean {
        return repository.existsByClientIdAndChecksum(clientId, checksum)
    }
}