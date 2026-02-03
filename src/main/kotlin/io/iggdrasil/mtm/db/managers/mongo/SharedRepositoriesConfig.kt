package io.iggdrasil.mtm.db.managers.mongo

import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = ["\${mtm.repositories.shared-packages}"],
    reactiveMongoTemplateRef = "sharedTemplate"
)
class SharedRepositoriesConfig {

    @Value("\${mtm.repositories.shared-database}")
    private lateinit var tenantDb: String

    @Bean
    fun sharedTemplate(
        @Value("\${spring.data.mongodb.uri}") uri: String,
    ): ReactiveMongoTemplate {
        val factory =
            SimpleReactiveMongoDatabaseFactory(
                MongoClients.create(uri),
                tenantDb
            )

        return ReactiveMongoTemplate(factory)
    }
}
