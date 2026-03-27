package io.iggdrasil.mtm.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtm")
data class MultiTenancyProperties(

    var dataSource: DataSourceProperties,

    var repositories: RepositoryPackagesProperties =
        RepositoryPackagesProperties()
)

