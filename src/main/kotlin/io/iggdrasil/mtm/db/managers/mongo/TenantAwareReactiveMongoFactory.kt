package io.iggdrasil.mtm.db.managers.mongo

import io.iggdrasil.mtm.config.props.DataSourceProperties
import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.CachedDbResource
import io.iggdrasil.mtm.tenant.TenantContext
import com.mongodb.ClientSessionOptions
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoDatabase
import jakarta.annotation.PreDestroy
import org.bson.codecs.configuration.CodecRegistry
import org.slf4j.LoggerFactory
import org.springframework.dao.support.PersistenceExceptionTranslator
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoExceptionTranslator
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

class TenantAwareReactiveMongoFactory(
    private val properties: MultiTenancyProperties
) : ReactiveMongoDatabaseFactory {

    private val log = LoggerFactory.getLogger(javaClass)

    private val clients = ConcurrentHashMap<String, CachedDbResource<MongoClient>>()

    val activeClients: Map<String, CachedDbResource<MongoClient>>
        get() = clients

    private val defaultUri by lazy { connectionString(properties.dataSource) }

    private val defaultClient by lazy { MongoClients.create(defaultUri) }
    private val defaultDb = properties.dataSource.database

    /**
     * O banco GLOBAL, fixo: não olha o tenant do contexto. É a base do template dos
     * repositórios `main`.
     */
    val globalDatabaseFactory: ReactiveMongoDatabaseFactory by lazy {
        SimpleReactiveMongoDatabaseFactory(defaultClient, defaultDb)
    }

    override fun getMongoDatabase(): Mono<MongoDatabase> {
        return Mono.deferContextual {
            TenantContext.read()
                .flatMap { tenantId ->
                    val cached = clients.computeIfAbsent(tenantId) {
                        log.info("Creating Mongo client tenant={}", tenantId)
                        CachedDbResource(MongoClients.create(defaultUri))
                    }

                    cached.touch()

                    val dbName = "$defaultDb-$tenantId"
                    log.debug("Using tenant Mongo database={} tenant={}", dbName, tenantId)

                    Mono.just(cached.resource.getDatabase(dbName))
                }
        }
            .switchIfEmpty(
                Mono.fromSupplier {
                    log.debug("Using GLOBAL Mongo database={}", defaultDb)
                    defaultClient.getDatabase(defaultDb)
                }
            )
    }

    /** O banco pedido PELO NOME, explícito, independente do tenant do contexto. */
    override fun getMongoDatabase(dbName: String): Mono<MongoDatabase> =
        Mono.fromSupplier { defaultClient.getDatabase(dbName) }

    override fun getCodecRegistry(): CodecRegistry = globalDatabaseFactory.codecRegistry
    override fun getExceptionTranslator(): PersistenceExceptionTranslator = MongoExceptionTranslator()
    override fun getSession(options: ClientSessionOptions) = globalDatabaseFactory.getSession(options)
    override fun withSession(session: ClientSession): ReactiveMongoDatabaseFactory = this

    @Scheduled(fixedDelay = 300000)
    fun cleanupClients() {
        val ttl = 30 * 60 * 1000L
        clients.entries.removeIf { (tenantId, cached) ->
            if (cached.isExpired(ttl)) {
                log.debug("Cleaning idle Mongo client tenant={}", tenantId)
                cached.resource.close()
                true
            } else false
        }
    }

    @PreDestroy
    fun shutdown() {
        log.info("Shutting down TenantAwareReactiveMongoFactory")
        clients.forEach { (tenantId, cached) ->
            try {
                log.info("Closing Mongo client tenant={}", tenantId)
                cached.resource.close()
            } catch (e: Exception) {
                log.warn("Error closing tenant {}", tenantId, e)
            }
        }
        clients.clear()
        defaultClient.close()
    }

    companion object {
        /** `mongodb://[user:senha@]host:porta`, com usuário e senha codificados para a URI. */
        internal fun connectionString(dataSource: DataSourceProperties): String {
            val credentials =
                if (dataSource.username.isNotBlank() && dataSource.password.isNotBlank()) {
                    "${encode(dataSource.username)}:${encode(dataSource.password)}@"
                } else {
                    ""
                }
            return "mongodb://$credentials${dataSource.host}:${dataSource.port}"
        }

        private fun encode(value: String): String =
            URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
    }
}
