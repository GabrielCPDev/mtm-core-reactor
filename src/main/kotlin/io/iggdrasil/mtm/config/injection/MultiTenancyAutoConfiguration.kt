package io.iggdrasil.mtm.config.injection

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.mongo.MongoMultiTenancyConfiguration
import io.iggdrasil.mtm.db.managers.postgres.R2dbcMultiTenancyConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration
import org.springframework.boot.data.r2dbc.autoconfigure.DataR2dbcAutoConfiguration
import org.springframework.boot.data.r2dbc.autoconfigure.DataR2dbcRepositoriesAutoConfiguration
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration
import org.springframework.boot.r2dbc.autoconfigure.R2dbcTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * Multi-tenancy reativo. Conforme `mtm.data-source.type`, liga o Mongo
 * ([MongoMultiTenancyConfiguration]) ou o R2DBC ([R2dbcMultiTenancyConfiguration]), cada um
 * com os escopos de repositório `main` (banco global fixo) e `tenant` (banco do tenant do
 * contexto).
 *
 * Roda ANTES das auto-configurações de dados do Spring Boot: assim os templates, a conexão e
 * os repositórios daqui valem e os do Boot recuam (`@ConditionalOnMissingBean`).
 */
@AutoConfiguration(
    before = [
        R2dbcAutoConfiguration::class,
        R2dbcTransactionManagerAutoConfiguration::class,
        DataR2dbcAutoConfiguration::class,
        DataR2dbcRepositoriesAutoConfiguration::class,
        DataMongoReactiveAutoConfiguration::class,
        DataMongoReactiveRepositoriesAutoConfiguration::class,
    ],
)
@EnableConfigurationProperties(MultiTenancyProperties::class)
@Import(MongoMultiTenancyConfiguration::class, R2dbcMultiTenancyConfiguration::class)
class MultiTenancyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        findAndRegisterModules()
    }
}
