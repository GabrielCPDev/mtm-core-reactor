package io.iggdrasil.mtm.db.managers.postgres

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "POSTGRES")
@EnableR2dbcRepositories(
    basePackages = ["\${mtm.repositories.shared-packages}"],
    entityOperationsRef = "sharedEntityTemplate"
)
class SharedR2dbcRepositoriesConfig {

    @Bean
    fun sharedEntityTemplate(
        connectionFactory: ConnectionFactory
    ): R2dbcEntityTemplate = R2dbcEntityTemplate(connectionFactory)
}
