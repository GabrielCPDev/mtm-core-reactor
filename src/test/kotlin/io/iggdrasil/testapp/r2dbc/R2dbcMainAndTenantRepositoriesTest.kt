package io.iggdrasil.testapp.r2dbc

import io.iggdrasil.mtm.tenant.TenantContext
import io.iggdrasil.testapp.r2dbc.main.MainRowRepository
import io.iggdrasil.testapp.r2dbc.tenant.TenantRowRepository
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import kotlin.test.assertEquals

/**
 * Os dois escopos de repositório no R2DBC, contra um Postgres de verdade (binários
 * embutidos do zonky, sem Docker). Três bancos: o global e os de dois tenants.
 */
@SpringBootTest(classes = [R2dbcTestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class R2dbcMainAndTenantRepositoriesTest {

    @Autowired lateinit var context: ApplicationContext
    @Autowired lateinit var mainRepository: MainRowRepository
    @Autowired lateinit var tenantRepository: TenantRowRepository

    @BeforeEach
    fun cleanTables() {
        DATABASES.forEach { execute(it, "TRUNCATE test_rows") }
    }

    @AfterAll
    fun stopPostgres() {
        postgres.close()
    }

    @Test
    fun `main repository writes to the global database even with a tenant in the context`() {
        mainRepository.save(TestRow(description = "main-1")).contextWrite(TenantContext.write(TENANT)).block()

        assertEquals(listOf("main-1"), descriptionsIn(GLOBAL_DATABASE))
        assertEquals(emptyList(), descriptionsIn(TENANT_DATABASE))
    }

    @Test
    fun `main repository reads from the global database even with a tenant in the context`() {
        val saved = mainRepository.save(TestRow(description = "main-2")).block()!!

        val found = mainRepository.findById(saved.id!!).contextWrite(TenantContext.write(TENANT)).block()

        assertEquals("main-2", found?.description)
    }

    @Test
    fun `tenant repository writes to the tenant database`() {
        tenantRepository.save(TestRow(description = "tenant-1")).contextWrite(TenantContext.write(TENANT)).block()

        assertEquals(listOf("tenant-1"), descriptionsIn(TENANT_DATABASE))
        assertEquals(emptyList(), descriptionsIn(GLOBAL_DATABASE))
    }

    @Test
    fun `tenant repository without a tenant in the context falls back to the global database`() {
        tenantRepository.save(TestRow(description = "tenant-2")).block()

        assertEquals(listOf("tenant-2"), descriptionsIn(GLOBAL_DATABASE))
    }

    @Test
    fun `each tenant has its own database`() {
        tenantRepository.save(TestRow(description = "a")).contextWrite(TenantContext.write(TENANT)).block()
        tenantRepository.save(TestRow(description = "b")).contextWrite(TenantContext.write(OTHER_TENANT)).block()

        assertEquals(listOf("a"), descriptionsIn(TENANT_DATABASE))
        assertEquals(listOf("b"), descriptionsIn(OTHER_TENANT_DATABASE))
    }

    @Test
    fun `there are exactly the two templates of the lib`() {
        assertEquals(
            setOf("tenantEntityTemplate", "mainEntityTemplate"),
            context.getBeanNamesForType(R2dbcEntityTemplate::class.java).toSet(),
        )
    }

    private fun descriptionsIn(database: String): List<String> =
        postgres.getDatabase("postgres", database).connection.use { connection ->
            connection.createStatement().executeQuery("SELECT description FROM test_rows ORDER BY description").use { rows ->
                generateSequence { if (rows.next()) rows.getString(1) else null }.toList()
            }
        }

    companion object {
        private const val GLOBAL_DATABASE = "mtm_it"
        private const val TENANT = "t1"
        private const val OTHER_TENANT = "t2"
        private const val TENANT_DATABASE = "$GLOBAL_DATABASE-$TENANT"
        private const val OTHER_TENANT_DATABASE = "$GLOBAL_DATABASE-$OTHER_TENANT"
        private val DATABASES = listOf(GLOBAL_DATABASE, TENANT_DATABASE, OTHER_TENANT_DATABASE)

        private val postgres: EmbeddedPostgres = EmbeddedPostgres.start().also { postgres ->
            DATABASES.forEach { database -> execute(postgres, "postgres", "CREATE DATABASE \"$database\"") }
            DATABASES.forEach { database ->
                execute(postgres, database, "CREATE TABLE test_rows (id BIGSERIAL PRIMARY KEY, description TEXT NOT NULL)")
            }
        }

        private fun execute(database: String, sql: String) = execute(postgres, database, sql)

        private fun execute(postgres: EmbeddedPostgres, database: String, sql: String) {
            postgres.getDatabase("postgres", database).connection.use { connection ->
                connection.createStatement().use { it.execute(sql) }
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun mtmProperties(registry: DynamicPropertyRegistry) {
            registry.add("mtm.data-source.type") { "POSTGRES" }
            registry.add("mtm.data-source.host") { "localhost" }
            registry.add("mtm.data-source.port") { postgres.port }
            registry.add("mtm.data-source.username") { "postgres" }
            registry.add("mtm.data-source.password") { "postgres" }
            registry.add("mtm.data-source.database") { GLOBAL_DATABASE }
            registry.add("mtm.data-source.max-pool-size") { "5" }
            registry.add("mtm.repositories.main[0]") { "io.iggdrasil.testapp.r2dbc.main" }
            registry.add("mtm.repositories.tenant[0]") { "io.iggdrasil.testapp.r2dbc.tenant" }
        }
    }
}
