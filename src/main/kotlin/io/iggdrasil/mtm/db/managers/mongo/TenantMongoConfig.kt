package io.iggdrasil.mtm.db.managers.mongo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MONGO")
@EnableReactiveMongoRepositories(
    basePackages = ["#{multiTenancyProperties.repositories.tenant}"],
    reactiveMongoTemplateRef = "reactiveMongoTemplate"
)
class TenantMongoConfig(private val tenantFactory: TenantAwareReactiveMongoFactory) {

    @Bean(name = ["reactiveMongoTemplate", "tenantTemplate"])
    @Primary
    fun reactiveMongoTemplate(): ReactiveMongoTemplate = ReactiveMongoTemplate(tenantFactory)
}
