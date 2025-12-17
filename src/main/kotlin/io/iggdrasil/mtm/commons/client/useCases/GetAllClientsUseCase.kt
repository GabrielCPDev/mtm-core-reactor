package io.iggdrasil.mtm.commons.client.useCases

import io.iggdrasil.mtm.commons.client.Client
import io.iggdrasil.mtm.commons.repository.ClientRepositoryPort
import web.pageable.PageResult
import web.pageable.Pagination

class GetAllClientsUseCase(
    private val repository: ClientRepositoryPort
) {
    suspend fun execute(
        page: Int = 0,
        size: Int = 20,
        sort: List<String>? = null
    ): PageResult<Client> {
        val pagination = Pagination.of(page, size, sort)
        return repository.findAll(pagination)
    }
}
