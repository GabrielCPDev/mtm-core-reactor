package io.iggdrasil.mtm.db.managers.postgres

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(
    basePackages = ["\${mtm.repositories.main-packages}"],
    entityOperationsRef = "mainEntityTemplate"
)
class MainR2dbcRepositoriesConfig {

    @Bean
    fun mainEntityTemplate(
        connectionFactory: ConnectionFactory
    ): R2dbcEntityTemplate = R2dbcEntityTemplate(connectionFactory)
}
