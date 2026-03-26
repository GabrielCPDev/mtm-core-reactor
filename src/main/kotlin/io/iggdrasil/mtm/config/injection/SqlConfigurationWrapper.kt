package io.iggdrasil.mtm.config.injection

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.postgres.MainR2dbcRepositoriesConfig
import io.iggdrasil.mtm.db.managers.postgres.SharedR2dbcRepositoriesConfig
import io.iggdrasil.mtm.db.managers.postgres.TenantAwareR2dbcConnectionFactory
import io.iggdrasil.mtm.db.managers.postgres.TenantR2dbcRepositoriesConfig
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

@Configuration
@Conditional(OnSqlCondition::class)
@Import(TenantR2dbcRepositoriesConfig::class, MainR2dbcRepositoriesConfig::class, SharedR2dbcRepositoriesConfig::class)
class SqlConfigurationWrapper {

    @Bean
    @Primary
    fun connectionFactory(properties: MultiTenancyProperties) =
        TenantAwareR2dbcConnectionFactory(properties)

    @Bean(name = ["tenantEntityTemplate"])
    @Primary
    fun tenantEntityTemplate(factory: TenantAwareR2dbcConnectionFactory) =
        R2dbcEntityTemplate(factory)

    @Bean(name = ["mainEntityTemplate"])
    fun mainEntityTemplate(connectionFactory: ConnectionFactory) =
        R2dbcEntityTemplate(connectionFactory)

    @Bean(name = ["sharedEntityTemplate"])
    fun sharedEntityTemplate(connectionFactory: ConnectionFactory) =
        R2dbcEntityTemplate(connectionFactory)
}

class OnSqlCondition : AnyNestedCondition(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "POSTGRES")
    class OnPostgres

    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MYSQL")
    class OnMysql
}
