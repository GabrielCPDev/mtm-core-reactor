package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import models.ID

class DownloadContractFileUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(contractId: ID): ByteArray? {
        return repository.loadFileData(contractId)
    }
}