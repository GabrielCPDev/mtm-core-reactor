package io.iggdrasil.testapp.mongo

import com.mongodb.reactivestreams.client.MongoClients
import io.iggdrasil.mtm.db.managers.mongo.TenantAwareReactiveMongoFactory
import io.iggdrasil.mtm.tenant.TenantContext
import io.iggdrasil.testapp.mongo.main.MainDocumentRepository
import io.iggdrasil.testapp.mongo.tenant.TenantDocumentRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Os dois escopos de repositório no Mongo, contra um Mongo de verdade.
 *
 * Mongo em `MTM_TEST_MONGO_HOST`/`MTM_TEST_MONGO_PORT` (padrão `localhost:27017`; na
 * esteira, o service `mongodb` do workflow). Cada execução usa bancos com nome único e os
 * apaga no fim.
 */
@SpringBootTest(classes = [MongoTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoMainAndTenantRepositoriesTest {

    @Autowired lateinit var context: ApplicationContext
    @Autowired lateinit var mainRepository: MainDocumentRepository
    @Autowired lateinit var tenantRepository: TenantDocumentRepository
    @Autowired lateinit var tenantMongoFactory: TenantAwareReactiveMongoFactory
    @Autowired lateinit var defaultTemplate: ReactiveMongoTemplate
    @Autowired @Qualifier("mainTemplate") lateinit var mainTemplate: ReactiveMongoTemplate

    private val mongoClient = MongoClients.create("mongodb://$HOST:$PORT")

    @BeforeEach
    fun cleanDatabases() {
        testDatabases().forEach { Mono.from(mongoClient.getDatabase(it).drop()).block() }
    }

    @AfterAll
    fun dropDatabasesAndClose() {
        cleanDatabases()
        mongoClient.close()
    }

    @Test
    fun `main repository writes to the global database even with a tenant in the context`() {
        mainRepository.save(TestDocument("main-1", "global"))
            .contextWrite(TenantContext.write(TENANT)).block()

        assertEquals(listOf("main-1"), idsIn(GLOBAL_DATABASE))
        assertEquals(emptyList(), idsIn(TENANT_DATABASE))
    }

    @Test
    fun `main repository reads from the global database even with a tenant in the context`() {
        mainRepository.save(TestDocument("main-2", "global")).block()

        val found = mainRepository.findById("main-2").contextWrite(TenantContext.write(TENANT)).block()

        assertEquals("main-2", found?.id)
    }

    @Test
    fun `tenant repository writes to the tenant database`() {
        tenantRepository.save(TestDocument("tenant-1", "do tenant"))
            .contextWrite(TenantContext.write(TENANT)).block()

        assertEquals(listOf("tenant-1"), idsIn(TENANT_DATABASE))
        assertEquals(emptyList(), idsIn(GLOBAL_DATABASE))
    }

    @Test
    fun `tenant repository without a tenant in the context falls back to the global database`() {
        tenantRepository.save(TestDocument("tenant-2", "sem tenant")).block()

        assertEquals(listOf("tenant-2"), idsIn(GLOBAL_DATABASE))
    }

    @Test
    fun `each tenant has its own database`() {
        tenantRepository.save(TestDocument("a", "do tenant")).contextWrite(TenantContext.write(TENANT)).block()
        tenantRepository.save(TestDocument("b", "do outro")).contextWrite(TenantContext.write(OTHER_TENANT)).block()

        assertEquals(listOf("a"), idsIn(TENANT_DATABASE))
        assertEquals(listOf("b"), idsIn(OTHER_TENANT_DATABASE))
    }

    @Test
    fun `the default template is the tenant one and the main template is fixed on the global database`() {
        assertSame(context.getBean("tenantTemplate"), defaultTemplate)
        assertEquals(TENANT_DATABASE, defaultTemplate.mongoDatabase.contextWrite(TenantContext.write(TENANT)).block()?.name)
        assertEquals(GLOBAL_DATABASE, mainTemplate.mongoDatabase.contextWrite(TenantContext.write(TENANT)).block()?.name)
        assertEquals(setOf("reactiveMongoTemplate", "mainTemplate"), context.getBeanNamesForType(ReactiveMongoTemplate::class.java).toSet())
    }

    @Test
    fun `asking a database by name returns that database, whatever the context`() {
        assertEquals("$GLOBAL_DATABASE-t9", tenantMongoFactory.getMongoDatabase("$GLOBAL_DATABASE-t9").block()?.name)
        assertEquals(
            "$GLOBAL_DATABASE-t9",
            tenantMongoFactory.getMongoDatabase("$GLOBAL_DATABASE-t9").contextWrite(TenantContext.write(TENANT)).block()?.name,
        )
    }

    private fun idsIn(database: String): List<String> =
        Flux.from(mongoClient.getDatabase(database).getCollection("test_documents").find())
            .map { it.getString("_id") }
            .collectList()
            .block()
            .orEmpty()
            .sorted()

    private fun testDatabases() = listOf(GLOBAL_DATABASE, TENANT_DATABASE, OTHER_TENANT_DATABASE)

    companion object {
        private val HOST = System.getenv("MTM_TEST_MONGO_HOST") ?: "localhost"
        private val PORT = System.getenv("MTM_TEST_MONGO_PORT") ?: "27017"

        private val GLOBAL_DATABASE = "mtm_it_" + UUID.randomUUID().toString().take(8)
        private const val TENANT = "t1"
        private const val OTHER_TENANT = "t2"
        private val TENANT_DATABASE = "$GLOBAL_DATABASE-$TENANT"
        private val OTHER_TENANT_DATABASE = "$GLOBAL_DATABASE-$OTHER_TENANT"

        @JvmStatic
        @DynamicPropertySource
        fun mtmProperties(registry: DynamicPropertyRegistry) {
            // A lib traz o starter do R2DBC: em modo MONGO, o consumidor desliga o R2DBC do Boot
            // (sem URL de R2DBC ele não sobe).
            registry.add("spring.autoconfigure.exclude") {
                "org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration," +
                    "org.springframework.boot.data.r2dbc.autoconfigure.DataR2dbcAutoConfiguration," +
                    "org.springframework.boot.data.r2dbc.autoconfigure.DataR2dbcRepositoriesAutoConfiguration," +
                    "org.springframework.boot.r2dbc.autoconfigure.R2dbcTransactionManagerAutoConfiguration"
            }
            registry.add("spring.mongodb.uri") { "mongodb://$HOST:$PORT/$GLOBAL_DATABASE" }
            registry.add("mtm.data-source.type") { "MONGO" }
            registry.add("mtm.data-source.host") { HOST }
            registry.add("mtm.data-source.port") { PORT }
            registry.add("mtm.data-source.database") { GLOBAL_DATABASE }
            registry.add("mtm.data-source.max-pool-size") { "10" }
            registry.add("mtm.repositories.main[0]") { "io.iggdrasil.testapp.mongo.main" }
            registry.add("mtm.repositories.tenant[0]") { "io.iggdrasil.testapp.mongo.tenant" }
        }
    }
}
