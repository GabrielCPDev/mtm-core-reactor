package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.contract.ContractFile
import io.iggdrasil.mtm.commons.contract.Sha256
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import models.ID
import models.Name
import utils.ResourceNotFoundException

import java.time.Instant

class UpdateContractFileUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(
        contractId: ID,
        fileName: String,
        contentType: String,
        data: ByteArray
    ): Contract {
        val existing = repository.findById(contractId)
            ?: throw ResourceNotFoundException("Contract with id $contractId not found")

        val checksum = Sha256.generate(data)

        val file = ContractFile(
            fileName = Name.of(fileName),
            contentType = contentType,
            fileSize = data.size.toLong(),
            checksum = checksum,
            data = data
        )

        val updated = existing.copy(
            file = file,
            uploadedAt = Instant.now()
        )

        return repository.saveWithFile(updated, data)
    }
}