package io.iggdrasil.mtm.config.injection

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.reactivestreams.client.MongoClients
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.mongo.MainRepositoriesConfig
import io.iggdrasil.mtm.db.managers.mongo.SharedRepositoriesConfig
import io.iggdrasil.mtm.db.managers.mongo.TenantAwareReactiveMongoFactory
import io.iggdrasil.mtm.db.managers.mongo.TenantMongoConfig
import io.iggdrasil.mtm.db.managers.postgres.MainR2dbcRepositoriesConfig
import io.iggdrasil.mtm.db.managers.postgres.SharedR2dbcRepositoriesConfig
import io.iggdrasil.mtm.db.managers.postgres.TenantAwareR2dbcConnectionFactory
import io.iggdrasil.mtm.db.managers.postgres.TenantR2dbcRepositoriesConfig
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

@AutoConfiguration(
    beforeName = [
        "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration",
        "org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
    ]
)
@EnableConfigurationProperties(MultiTenancyProperties::class)
@Import(
    MultiTenancyAutoConfiguration.MongoConfigurationWrapper::class,
    MultiTenancyAutoConfiguration.SqlConfigurationWrapper::class
)
class MultiTenancyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        findAndRegisterModules()
    }

    @Configuration
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MONGO")
    @Import(TenantMongoConfig::class, MainRepositoriesConfig::class, SharedRepositoriesConfig::class)
    class MongoConfigurationWrapper(private val properties: MultiTenancyProperties) {

        @Bean
        fun tenantMongoFactory() = TenantAwareReactiveMongoFactory(properties)

        @Bean(name = ["reactiveMongoTemplate", "tenantTemplate"])
        @Primary
        fun reactiveMongoTemplate(factory: TenantAwareReactiveMongoFactory) =
            ReactiveMongoTemplate(factory)

        @Bean
        fun mainTemplate(): ReactiveMongoTemplate {
            val ds = properties.dataSource
            val uri = "mongodb://${ds.host}:${ds.port}"
            val factory = SimpleReactiveMongoDatabaseFactory(MongoClients.create(uri), ds.database)
            return ReactiveMongoTemplate(factory)
        }

        @Bean
        fun sharedTemplate(): ReactiveMongoTemplate {
            val ds = properties.dataSource
            val uri = "mongodb://${ds.host}:${ds.port}"
            val sharedDb = properties.repositories.shared.firstOrNull()?.split(".")?.last() ?: "shared_db"
            val factory = SimpleReactiveMongoDatabaseFactory(MongoClients.create(uri), sharedDb)
            return ReactiveMongoTemplate(factory)
        }
    }

    @Configuration
    @Conditional(OnSqlCondition::class)
    @Import(
        TenantR2dbcRepositoriesConfig::class,
        MainR2dbcRepositoriesConfig::class,
        SharedR2dbcRepositoriesConfig::class
    )
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
}

class OnSqlCondition : AnyNestedCondition(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "POSTGRES")
    class OnPostgres

    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MYSQL")
    class OnMysql
}