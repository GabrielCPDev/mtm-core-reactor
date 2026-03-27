package io.iggdrasil.mtm.config.injection

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.mongo.TenantAwareReactiveMongoFactory
import io.iggdrasil.mtm.db.managers.postgres.TenantAwareR2dbcConnectionFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration
import org.springframework.boot.r2dbc.autoconfigure.R2dbcTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration(before = [R2dbcAutoConfiguration::class, R2dbcTransactionManagerAutoConfiguration::class])
@AutoConfigureBefore(R2dbcAutoConfiguration::class)
@EnableConfigurationProperties(MultiTenancyProperties::class)
class MultiTenancyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        findAndRegisterModules()
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "mtm.data-source",
        name = ["type"],
        havingValue = "MONGO"
    )
    fun tenantMongoFactory(
        properties: MultiTenancyProperties
    ): TenantAwareReactiveMongoFactory =
        TenantAwareReactiveMongoFactory(properties)

    @Bean
    @ConditionalOnProperty(
        prefix = "mtm.data-source",
        name = ["type"],
        havingValue = "POSTGRES"
    )
    fun tenantConnectionFactoryPostgres(
        properties: MultiTenancyProperties
    ) = TenantAwareR2dbcConnectionFactory(properties)

    @Bean
    @ConditionalOnProperty(
        prefix = "mtm.data-source",
        name = ["type"],
        havingValue = "MYSQL"
    )
    fun tenantConnectionFactoryMysql(
        properties: MultiTenancyProperties
    ) = TenantAwareR2dbcConnectionFactory(properties)
}
