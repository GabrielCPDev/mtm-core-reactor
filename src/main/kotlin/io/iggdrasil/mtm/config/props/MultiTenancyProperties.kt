package io.iggdrasil.mtm.config.props

import io.iggdrasil.mtm.tenant.DataSourceType
import io.iggdrasil.mtm.tenant.TenancyDBStrategy
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtm")
data class MultiTenancyProperties(
    var dataSource: DataSourceProperties = DataSourceProperties(),
    val strategy: TenancyDBStrategy = TenancyDBStrategy.SCHEMA
) {

    init {
        require(dataSource.username.isNotBlank()) {
            "mtm.data-source.username is required"
        }

        require(dataSource.password.isNotBlank()) {
            "mtm.data-source.password is required"
        }

        require(
            !(dataSource.type == DataSourceType.MONGO &&
                    strategy == TenancyDBStrategy.SCHEMA)
        ) {
            "SCHEMA strategy is not supported for MongoDB"
        }
    }
}
