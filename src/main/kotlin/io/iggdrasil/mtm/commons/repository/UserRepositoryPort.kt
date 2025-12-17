package io.iggdrasil.mtm.commons.repository

import db.repositories.BaseRepository
import io.iggdrasil.mtm.commons.user.User
import kotlinx.coroutines.flow.Flow
import models.Email
import models.ID

interface UserRepositoryPort : BaseRepository<User, ID> {

    suspend fun findByEmail(email: Email): User?

    suspend fun existsByEmail(email: Email): Boolean

    fun findAllByEnabled(enabled: Boolean, page: Int = 0, size: Int = 20): Flow<User>

}