package io.iggdrasil.mtm.db.managers.postgres

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(
    basePackages = ["\${mtm.repositories.tenant-packages}"],
    entityOperationsRef = "tenantEntityTemplate"
)
class TenantR2dbcRepositoriesConfig {

    @Bean
    @Primary
    fun tenantEntityTemplate(
        tenantConnectionFactory: TenantAwareConnectionFactory
    ): R2dbcEntityTemplate = R2dbcEntityTemplate(tenantConnectionFactory)
}
