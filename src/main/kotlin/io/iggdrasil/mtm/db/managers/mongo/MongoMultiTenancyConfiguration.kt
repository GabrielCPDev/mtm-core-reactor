package io.iggdrasil.mtm.db.managers.mongo

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.ScopedRepositoriesRegistrar
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.data.mongodb.repository.config.ReactiveMongoRepositoryConfigurationExtension

/**
 * Mongo reativo (`mtm.data-source.type: MONGO`), com dois escopos de repositório:
 *
 * - `main` ([MONGO_MAIN_TEMPLATE]): banco GLOBAL fixo, com ou sem tenant no contexto;
 * - `tenant` ([MONGO_TENANT_TEMPLATE], o `@Primary`): banco `<database>-<tenant>` do
 *   contexto; sem tenant, o global.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MONGO")
@Import(MongoMainRepositoriesRegistrar::class, MongoTenantRepositoriesRegistrar::class)
class MongoMultiTenancyConfiguration {

    @Bean
    fun tenantMongoFactory(properties: MultiTenancyProperties): TenantAwareReactiveMongoFactory =
        TenantAwareReactiveMongoFactory(properties)

    /** Banco do tenant do contexto. É o template padrão da aplicação. */
    @Bean(name = [MONGO_TENANT_TEMPLATE, "tenantTemplate"])
    @Primary
    fun reactiveMongoTemplate(
        tenantMongoFactory: TenantAwareReactiveMongoFactory,
        mongoConverter: ObjectProvider<MongoConverter>,
    ): ReactiveMongoTemplate = template(tenantMongoFactory, mongoConverter)

    /** Banco global fixo: não olha o tenant do contexto. */
    @Bean(name = [MONGO_MAIN_TEMPLATE])
    fun mainTemplate(
        tenantMongoFactory: TenantAwareReactiveMongoFactory,
        mongoConverter: ObjectProvider<MongoConverter>,
    ): ReactiveMongoTemplate = template(tenantMongoFactory.globalDatabaseFactory, mongoConverter)

    /** Usa o conversor do Spring Boot (conversões customizadas da aplicação) quando existir. */
    private fun template(
        databaseFactory: ReactiveMongoDatabaseFactory,
        mongoConverter: ObjectProvider<MongoConverter>,
    ): ReactiveMongoTemplate =
        mongoConverter.ifAvailable
            ?.let { ReactiveMongoTemplate(databaseFactory, it) }
            ?: ReactiveMongoTemplate(databaseFactory)
}

/** Template Mongo fixo no banco global. */
const val MONGO_MAIN_TEMPLATE = "mainTemplate"

/** Template Mongo que escolhe o banco pelo tenant do contexto (o `@Primary`). */
const val MONGO_TENANT_TEMPLATE = "reactiveMongoTemplate"

/** Repositórios `mtm.repositories.main` no Mongo: sempre no banco global. */
class MongoMainRepositoriesRegistrar :
    ScopedRepositoriesRegistrar("main", Settings::class.java, EnableReactiveMongoRepositories::class.java) {
    override fun extension() = ReactiveMongoRepositoryConfigurationExtension()

    @EnableReactiveMongoRepositories(reactiveMongoTemplateRef = MONGO_MAIN_TEMPLATE)
    private class Settings
}

/** Repositórios `mtm.repositories.tenant` no Mongo: no banco do tenant do contexto. */
class MongoTenantRepositoriesRegistrar :
    ScopedRepositoriesRegistrar("tenant", Settings::class.java, EnableReactiveMongoRepositories::class.java) {
    override fun extension() = ReactiveMongoRepositoryConfigurationExtension()

    @EnableReactiveMongoRepositories(reactiveMongoTemplateRef = MONGO_TENANT_TEMPLATE)
    private class Settings
}
