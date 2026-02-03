package io.iggdrasil.mtm.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtm")
data class MultiTenancyProperties(

    var dataSource: DataSourceProperties = DataSourceProperties(),

    var repositories: RepositoryPackagesProperties =
        RepositoryPackagesProperties()
) {

    init {
        require(dataSource.username.isNotBlank()) {
            "mtm.data-source.username is required"
        }

        require(dataSource.password.isNotBlank()) {
            "mtm.data-source.password is required"
        }
    }
}

