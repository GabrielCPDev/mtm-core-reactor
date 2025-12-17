package io.iggdrasil.mtm.commons.repository

import db.repositories.BaseRepository
import io.iggdrasil.mtm.commons.tenant.Tenant
import kotlinx.coroutines.flow.Flow
import models.ID

interface TenantRepositoryPort : BaseRepository<Tenant, ID> {

    suspend fun findByClientId(clientId: ID): Tenant?
    fun findAllActive(): Flow<Tenant>
}