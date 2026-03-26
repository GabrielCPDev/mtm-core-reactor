package io.iggdrasil.mtm.config.injection

import com.mongodb.reactivestreams.client.MongoClients
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.mongo.MainRepositoriesConfig
import io.iggdrasil.mtm.db.managers.mongo.SharedRepositoriesConfig
import io.iggdrasil.mtm.db.managers.mongo.TenantAwareReactiveMongoFactory
import io.iggdrasil.mtm.db.managers.mongo.TenantMongoConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory

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