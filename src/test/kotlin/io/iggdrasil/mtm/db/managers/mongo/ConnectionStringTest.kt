package io.iggdrasil.mtm.db.managers.mongo

import io.iggdrasil.mtm.config.props.DataSourceProperties
import io.iggdrasil.mtm.tenant.DataSourceType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConnectionStringTest {

    private fun dataSource(username: String = "", password: String = "") = DataSourceProperties(
        type = DataSourceType.MONGO,
        host = "db.local",
        port = 27017,
        username = username,
        password = password,
        database = "app",
        maxPoolSize = 10,
    )

    @Test
    fun `without credentials it is only host and port`() {
        assertEquals("mongodb://db.local:27017", TenantAwareReactiveMongoFactory.connectionString(dataSource()))
    }

    @Test
    fun `credentials go encoded into the uri`() {
        assertEquals(
            "mongodb://app%40user:p%40ss%3Aw%2Fo%20rd@db.local:27017",
            TenantAwareReactiveMongoFactory.connectionString(dataSource("app@user", "p@ss:w/o rd")),
        )
    }
}
