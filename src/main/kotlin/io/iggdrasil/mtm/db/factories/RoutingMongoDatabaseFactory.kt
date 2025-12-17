package io.iggdrasil.mtm.db.factories

import com.mongodb.ClientSessionOptions
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoDatabase
import io.iggdrasil.mtm.db.managers.DataSourceManagerMongo
import kotlinx.coroutines.reactor.mono
import org.bson.codecs.configuration.CodecRegistry
import org.springframework.dao.support.PersistenceExceptionTranslator
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoExceptionTranslator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RoutingMongoDatabaseFactory(
    private val dataSourceManager: DataSourceManagerMongo
) : ReactiveMongoDatabaseFactory {

    private val exceptionTranslator = MongoExceptionTranslator()

    /**
     * Esta é a função principal que o Spring chama.
     * Ela resolve se deve usar o banco do Tenant ou o banco Global.
     */
    override fun getMongoDatabase(): Mono<MongoDatabase> {
        return Mono.deferContextual { ctx ->
            val scope = ctx.getOrDefault("repo-scope", "TENANT") as String

            mono {
                if (scope == "GLOBAL") {
                    dataSourceManager.getGlobalMongoDatabase()
                } else {
                    dataSourceManager.getCurrentMongoDatabase()
                }
            }
        }
    }

    /**
     * Corrigido: Agora aponta para a implementação principal acima.
     * Antes havia um erro de recursividade aqui.
     */
    override fun getMongoDatabase(dbName: String): Mono<MongoDatabase> {
        return getMongoDatabase()
    }

    override fun getExceptionTranslator(): PersistenceExceptionTranslator =
        exceptionTranslator

    /**
     * Nota: Se o seu `getGlobalMongoDatabase` for uma suspend function,
     * você terá um problema aqui, pois a interface exige um retorno síncrono.
     * O ideal é que o manager exponha o codecRegistry de forma síncrona.
     */
    override fun getCodecRegistry(): CodecRegistry {

        return dataSourceManager.getGlobalMongoDatabase().codecRegistry
    }

    override fun getSession(options: ClientSessionOptions): Mono<ClientSession> =
        Mono.empty()

    override fun withSession(session: ClientSession): ReactiveMongoDatabaseFactory =
        this
}