package io.iggdrasil.mtm.config.injection

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.config.providers.TenantProvider
import io.iggdrasil.mtm.db.factories.RoutingConnectionFactory
import io.iggdrasil.mtm.db.factories.RoutingMongoDatabaseFactory
import io.iggdrasil.mtm.db.managers.DataSourceManagerMongo
import io.iggdrasil.mtm.db.managers.DataSourceManagerR2dbc
import io.iggdrasil.mtm.db.providers.mongo.MongoReactiveProvider
import io.iggdrasil.mtm.db.providers.mysql.MysqlR2dbcProvider
import io.iggdrasil.mtm.db.providers.pg.PostgresR2dbcProvider
import io.iggdrasil.mtm.tenant.TenantContextHolder
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory

@AutoConfiguration
@EnableConfigurationProperties(MultiTenancyProperties::class)
class MultiTenancyAutoConfiguration {

    @Bean
    fun tenantContextHolder() = TenantContextHolder()

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper =
        ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            findAndRegisterModules()
        }

    @Bean
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "POSTGRES")
    fun postgresConnectionProvider(
        properties: MultiTenancyProperties
    ): ConnectionProvider<ConnectionFactory> =
        PostgresR2dbcProvider(properties)

    @Bean
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MYSQL")
    fun mysqlConnectionProvider(
        properties: MultiTenancyProperties
    ): ConnectionProvider<ConnectionFactory> =
        MysqlR2dbcProvider(properties)

    @Bean
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MONGO")
    fun mongoConnectionProvider(
        properties: MultiTenancyProperties
    ): ConnectionProvider<MongoDatabase> =
        MongoReactiveProvider(properties)

    @Bean
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "POSTGRES")
    fun dataSourceManagerPostgres(
        tenantContextHolder: TenantContextHolder,
        properties: MultiTenancyProperties,
        provider: ConnectionProvider<ConnectionFactory>
    ): DataSourceManagerR2dbc =
        DataSourceManagerR2dbc(tenantContextHolder, properties, provider)

    @Bean
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MYSQL")
    fun dataSourceManagerMysql(
        tenantContextHolder: TenantContextHolder,
        properties: MultiTenancyProperties,
        provider: ConnectionProvider<ConnectionFactory>
    ): DataSourceManagerR2dbc =
        DataSourceManagerR2dbc(tenantContextHolder, properties, provider)

    @Bean
    @ConditionalOnProperty(prefix = "mtm.data-source", name = ["type"], havingValue = "MONGO")
    fun dataSourceManagerMongo(
        tenantContextHolder: TenantContextHolder,
        properties: MultiTenancyProperties,
        provider: ConnectionProvider<MongoDatabase>
    ): DataSourceManagerMongo =
        DataSourceManagerMongo(tenantContextHolder, properties, provider)

    @Bean
    @ConditionalOnBean(DataSourceManagerR2dbc::class)
    fun routingConnectionFactory(
        dataSourceManager: DataSourceManagerR2dbc
    ): RoutingConnectionFactory =
        RoutingConnectionFactory(dataSourceManager)

    @Bean
    @Primary
    @ConditionalOnBean(RoutingConnectionFactory::class)
    fun connectionFactory(
        routing: RoutingConnectionFactory
    ): ConnectionFactory = routing


    @Primary
    @Bean(name = ["mongoDatabaseFactory", "routingMongoDatabaseFactory"])
    @ConditionalOnBean(DataSourceManagerMongo::class)
    fun routingMongoDatabaseFactory(
        manager: DataSourceManagerMongo
    ): ReactiveMongoDatabaseFactory =
        RoutingMongoDatabaseFactory(manager)
}