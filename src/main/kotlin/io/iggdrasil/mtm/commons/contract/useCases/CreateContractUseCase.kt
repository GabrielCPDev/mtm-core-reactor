package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import models.ID
import java.time.Instant

class CreateContractUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(
        clientId: ID,
        extraInstances: Int = 0,
        expiresAt: Instant? = null
    ): Contract {
        val contract = Contract.create(
            clientId = clientId,
            extraInstances = extraInstances,
            expiresAt = expiresAt
        )

        return repository.save(contract)
    }
}
