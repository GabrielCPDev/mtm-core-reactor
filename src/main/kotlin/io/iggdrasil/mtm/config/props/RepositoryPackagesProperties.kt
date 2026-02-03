package io.iggdrasil.mtm.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mtm.repositories")
data class RepositoryPackagesProperties(

    var main: List<String> = emptyList(),
    var shared: List<String> = emptyList(),
    var tenant: List<String> = emptyList()
)
