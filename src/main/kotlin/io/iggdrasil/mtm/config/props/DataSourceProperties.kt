package io.iggdrasil.mtm.config.props

import io.iggdrasil.mtm.tenant.DataSourceType

data class DataSourceProperties(
    var type: DataSourceType = DataSourceType.POSTGRES,
    var host: String = "localhost",
    var port: Int = 5432,
    var username: String = "",
    var password: String = "",
    var database: String = "",
    var maxPoolSize: Int = 10
)