package io.iggdrasil.mtm.config.props


import io.iggdrasil.mtm.commons.tenant.DataSourceType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtm")
data class MultiTenancyProperties(
    var dataSource: DataSourceProperties = DataSourceProperties(),
    var tenant: TenantProperties = TenantProperties()
) {
    data class DataSourceProperties(
        var type: DataSourceType = DataSourceType.POSTGRES,
        var host: String = "localhost",
        var port: Int = 5432,
        var username: String = "",
        var password: String = "",
        var database: String = "",
        var maxPoolSize: Int = 10
    )

    companion object {
        internal const val MANAGER_URL = "https://api.multi-tenancy-manager.com"
    }

    fun validate() {
        require(dataSource.username.isNotBlank()) { "mtm.data-source.username is required" }
        require(dataSource.password.isNotBlank()) { "mtm.data-source.password is required" }
    }
}