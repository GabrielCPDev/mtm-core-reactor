package io.iggdrasil.mtm.db.managers.mongo

import com.mongodb.reactivestreams.client.MongoClients
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MONGO")
@EnableReactiveMongoRepositories(
    basePackages = ["#{multiTenancyProperties.repositories.shared}"],
    reactiveMongoTemplateRef = "sharedTemplate"
)
class SharedRepositoriesConfig(private val properties: MultiTenancyProperties) {

    @Bean
    fun sharedTemplate(): ReactiveMongoTemplate {
        val ds = properties.dataSource
        val uri = "mongodb://${ds.host}:${ds.port}"
        val sharedDb = properties.repositories.shared.firstOrNull()?.split(".")?.last() ?: "shared_db"
        val factory = SimpleReactiveMongoDatabaseFactory(MongoClients.create(uri), sharedDb)
        return ReactiveMongoTemplate(factory)
    }
}
