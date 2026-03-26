package io.iggdrasil.mtm.config.injection

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.mongo.TenantAwareReactiveMongoFactory
import io.iggdrasil.mtm.db.managers.postgres.TenantAwareR2dbcConnectionFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@AutoConfiguration(
    before = [
        MongoReactiveAutoConfiguration::class,
        R2dbcAutoConfiguration::class
    ]
)
@EnableConfigurationProperties(MultiTenancyProperties::class)
class MultiTenancyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper =
        ObjectMapper().apply {
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
    @Primary
    @ConditionalOnProperty(
        prefix = "mtm.data-source",
        name = ["type"],
        havingValue = "MONGO"
    )
    fun reactiveMongoTemplate(
        factory: TenantAwareReactiveMongoFactory
    ): ReactiveMongoTemplate =
        ReactiveMongoTemplate(factory)

    @Bean
    @Primary
    @ConditionalOnProperty(
        prefix = "mtm.data-source",
        name = ["type"],
        havingValue = "POSTGRES"
    )
    fun connectionFactory(
        properties: MultiTenancyProperties
    ): TenantAwareR2dbcConnectionFactory =
        TenantAwareR2dbcConnectionFactory(properties)

    @Bean
    @ConditionalOnProperty(
        prefix = "mtm.data-source",
        name = ["type"],
        havingValue = "MYSQL"
    )
    fun tenantConnectionFactoryMysql(
        properties: MultiTenancyProperties
    ): TenantAwareR2dbcConnectionFactory =
        TenantAwareR2dbcConnectionFactory(properties)
}