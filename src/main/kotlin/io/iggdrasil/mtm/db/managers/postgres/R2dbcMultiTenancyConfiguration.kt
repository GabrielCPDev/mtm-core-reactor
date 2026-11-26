package io.iggdrasil.mtm.db.managers.postgres

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.ScopedRepositoriesRegistrar
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.r2dbc.repository.config.R2dbcRepositoryConfigurationExtension

/**
 * Banco relacional via R2DBC (`mtm.data-source.type: POSTGRES` ou `MYSQL`), com dois escopos
 * de repositório:
 *
 * - `main` ([R2DBC_MAIN_TEMPLATE]): pool do banco GLOBAL, fixo, com ou sem tenant no contexto;
 * - `tenant` ([R2DBC_TENANT_TEMPLATE], o `@Primary`): conexão do banco `<database>-<tenant>`
 *   do contexto; sem tenant, o global.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("'\${mtm.data-source.type:}' == 'POSTGRES' or '\${mtm.data-source.type:}' == 'MYSQL'")
@Import(R2dbcMainRepositoriesRegistrar::class, R2dbcTenantRepositoriesRegistrar::class)
class R2dbcMultiTenancyConfiguration {

    @Bean
    fun tenantConnectionFactory(properties: MultiTenancyProperties): TenantAwareR2dbcConnectionFactory =
        TenantAwareR2dbcConnectionFactory(properties)

    /** Conexão do tenant do contexto. É o template padrão da aplicação. */
    @Bean(name = [R2DBC_TENANT_TEMPLATE])
    @Primary
    fun tenantEntityTemplate(tenantConnectionFactory: TenantAwareR2dbcConnectionFactory): R2dbcEntityTemplate =
        R2dbcEntityTemplate(tenantConnectionFactory)

    /** Pool do banco global, fixo: não olha o tenant do contexto. */
    @Bean(name = [R2DBC_MAIN_TEMPLATE])
    fun mainEntityTemplate(tenantConnectionFactory: TenantAwareR2dbcConnectionFactory): R2dbcEntityTemplate =
        R2dbcEntityTemplate(tenantConnectionFactory.globalConnectionFactory)
}

/** Template R2DBC fixo no banco global. */
const val R2DBC_MAIN_TEMPLATE = "mainEntityTemplate"

/** Template R2DBC que escolhe o banco pelo tenant do contexto (o `@Primary`). */
const val R2DBC_TENANT_TEMPLATE = "tenantEntityTemplate"

/** Repositórios `mtm.repositories.main` no R2DBC: sempre no banco global. */
class R2dbcMainRepositoriesRegistrar :
    ScopedRepositoriesRegistrar("main", Settings::class.java, EnableR2dbcRepositories::class.java) {
    override fun extension() = R2dbcRepositoryConfigurationExtension()

    @EnableR2dbcRepositories(entityOperationsRef = R2DBC_MAIN_TEMPLATE)
    private class Settings
}

/** Repositórios `mtm.repositories.tenant` no R2DBC: no banco do tenant do contexto. */
class R2dbcTenantRepositoriesRegistrar :
    ScopedRepositoriesRegistrar("tenant", Settings::class.java, EnableR2dbcRepositories::class.java) {
    override fun extension() = R2dbcRepositoryConfigurationExtension()

    @EnableR2dbcRepositories(entityOperationsRef = R2DBC_TENANT_TEMPLATE)
    private class Settings
}
