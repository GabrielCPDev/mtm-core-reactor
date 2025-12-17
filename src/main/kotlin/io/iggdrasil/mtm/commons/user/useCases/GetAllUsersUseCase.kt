package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import io.iggdrasil.mtm.commons.user.User
import web.pageable.PageResult
import web.pageable.Pagination

class GetAllUsersUseCase(
    private val repository: UserRepositoryPort
) {
    suspend fun execute(
        page: Int = 0,
        size: Int = 20,
        sort: List<String>? = null
    ): PageResult<User> {
        val pagination = Pagination.of(page, size, sort)
        return repository.findAll(pagination)
    }
}
