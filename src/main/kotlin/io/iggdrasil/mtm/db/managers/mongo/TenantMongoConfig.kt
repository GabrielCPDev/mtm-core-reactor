package io.iggdrasil.mtm.db.managers.mongo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = ["\${mtm.repositories.tenant-packages}"],
    reactiveMongoTemplateRef = "tenantTemplate"
)
class TenantMongoConfig(
    private val tenantFactory: TenantAwareReactiveMongoFactory
) {

    @Bean
    @Primary
    fun tenantTemplate(): ReactiveMongoTemplate =
        ReactiveMongoTemplate(tenantFactory)
}
