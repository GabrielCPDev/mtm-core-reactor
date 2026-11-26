package io.iggdrasil.testapp.r2dbc.tenant

import io.iggdrasil.testapp.r2dbc.TestRow
import org.springframework.data.r2dbc.repository.R2dbcRepository

/** Pacote listado em `mtm.repositories.tenant`. */
interface TenantRowRepository : R2dbcRepository<TestRow, Long>
