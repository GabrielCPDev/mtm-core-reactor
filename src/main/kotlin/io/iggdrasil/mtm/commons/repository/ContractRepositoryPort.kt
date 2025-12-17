package io.iggdrasil.mtm.commons.repository

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.contract.Sha256
import db.repositories.BaseRepository
import kotlinx.coroutines.flow.Flow
import models.ID

interface ContractRepositoryPort : BaseRepository<Contract, ID> {

    fun findByClientId(clientId: ID, page: Int = 0, size: Int = 20): Flow<Contract>

    suspend fun existsByClientIdAndChecksum(clientId: ID, checksum: Sha256): Boolean

    suspend fun loadFileData(contractId: ID): ByteArray?        // para download do PDF

    suspend fun saveWithFile(contract: Contract, data: ByteArray): Contract
}