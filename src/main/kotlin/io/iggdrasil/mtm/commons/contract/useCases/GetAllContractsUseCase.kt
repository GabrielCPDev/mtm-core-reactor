package io.iggdrasil.mtm.commons.contract.useCases

import io.iggdrasil.mtm.commons.contract.Contract
import io.iggdrasil.mtm.commons.repository.ContractRepositoryPort
import web.pageable.PageResult
import web.pageable.Pagination

class GetAllContractsUseCase(
    private val repository: ContractRepositoryPort
) {
    suspend fun execute(
        page: Int = 0,
        size: Int = 20,
        sort: List<String>? = null
    ): PageResult<Contract> {
        val pagination = Pagination.of(page, size, sort)
        return repository.findAll(pagination)
    }
}
