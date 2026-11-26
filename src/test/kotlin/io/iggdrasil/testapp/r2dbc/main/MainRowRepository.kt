package io.iggdrasil.testapp.r2dbc.main

import io.iggdrasil.testapp.r2dbc.TestRow
import org.springframework.data.r2dbc.repository.R2dbcRepository

/** Pacote listado em `mtm.repositories.main`. */
interface MainRowRepository : R2dbcRepository<TestRow, Long>
