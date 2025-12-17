package io.iggdrasil.mtm.commons.repository

import io.iggdrasil.mtm.commons.client.ApiKey
import io.iggdrasil.mtm.commons.client.Client
import db.repositories.BaseRepository
import kotlinx.coroutines.flow.Flow
import models.ID

interface ClientRepositoryPort : BaseRepository<Client, ID> {

    fun findByUserId(userId: ID): Flow<Client>
    suspend fun findByApiKey(apiKey: ApiKey): Client?
    suspend fun existsByNameAndUserId(name: String, userId: ID): Boolean
    suspend fun existsByApiKey(apiKey: ApiKey): Boolean
}