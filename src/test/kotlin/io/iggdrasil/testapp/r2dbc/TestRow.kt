package io.iggdrasil.testapp.r2dbc

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("test_rows")
data class TestRow(@Id val id: Long? = null, val description: String)
