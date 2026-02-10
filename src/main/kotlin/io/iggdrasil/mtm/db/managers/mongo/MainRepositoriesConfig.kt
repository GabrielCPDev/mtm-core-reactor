package io.iggdrasil.mtm.db.managers.mongo

import com.mongodb.reactivestreams.client.MongoClients
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
    basePackages = ["\${mtm.repositories.main-packages}"],
    reactiveMongoTemplateRef = "mainTemplate"
)
class MainRepositoriesConfig {

    @Bean
    fun mainTemplate(
        @Value("\${spring.data.mongodb.uri}") uri: String,
        @Value("\${spring.data.mongodb.database}") db: String
    ): ReactiveMongoTemplate {
        val factory =
            SimpleReactiveMongoDatabaseFactory(
                MongoClients.create(uri),
                db
            )

        return ReactiveMongoTemplate(factory)
    }
}
