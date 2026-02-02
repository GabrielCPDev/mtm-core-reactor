package io.iggdrasil.mtm.db.factories

import com.mongodb.ClientSessionOptions
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.db.managers.DataSourceManagerMongo
import org.bson.codecs.configuration.CodecRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.support.PersistenceExceptionTranslator
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoExceptionTranslator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RoutingMongoDatabaseFactory(
    private val dataSourceManager: DataSourceManagerMongo
) : ReactiveMongoDatabaseFactory {

    private val log: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val exceptionTranslator = MongoExceptionTranslator()

    override fun getMongoDatabase(): Mono<MongoDatabase> {
        return Mono.deferContextual { ctx ->

            val scope = ctx.getOrDefault("repo-scope", "TENANT") as String

            val tenantId =
                ctx.getOrEmpty<String>("tenant-id")
                    .orElse(null)

            val db =
                if (scope == "GLOBAL") {
                    log.debug("Using GLOBAL Mongo database")
                    dataSourceManager.getGlobalMongoDatabase()
                } else {
                    log.debug("Using TENANT Mongo database tenant={}", tenantId)
                    dataSourceManager.getDatabaseForTenant(tenantId)
                }

            Mono.just(db)
        }
    }

    override fun getMongoDatabase(dbName: String): Mono<MongoDatabase> =
        getMongoDatabase()

    override fun getExceptionTranslator(): PersistenceExceptionTranslator =
        exceptionTranslator

    override fun getCodecRegistry(): CodecRegistry =
        dataSourceManager.getGlobalMongoDatabase().codecRegistry

    override fun getSession(options: ClientSessionOptions): Mono<ClientSession> =
        Mono.empty()

    override fun withSession(session: ClientSession): ReactiveMongoDatabaseFactory =
        this
}
