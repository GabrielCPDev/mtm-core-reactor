package io.iggdrasil.mtm.config.props

import io.iggdrasil.mtm.tenant.DataSourceType

data class DataSourceProperties(
    var type: DataSourceType,
    var host: String,
    var port: Int,
    var username: String,
    var password: String,
    var database: String,
    var maxPoolSize: Int
)