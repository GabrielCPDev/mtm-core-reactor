package io.iggdrasil.mtm.db.factories

import io.iggdrasil.mtm.db.managers.DataSourceManagerR2dbc
import io.iggdrasil.mtm.tenant.TenantContext
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RoutingConnectionFactory(
    private val dataSourceManager: DataSourceManagerR2dbc
) : ConnectionFactory {

    private val log: Logger by lazy {
        LoggerFactory.getLogger(RoutingConnectionFactory::class.java)
    }

    override fun create(): Mono<out Connection> =
        Mono.deferContextual { ctx ->

            val scope = ctx.getOrDefault("repo-scope", "TENANT") as String
            val tenantId = ctx.getOrEmpty<String>("tenant-id").orElse(null)

            if (scope == "GLOBAL") {
                log.debug("Using GLOBAL R2DBC connection")
                Mono.from(dataSourceManager.getGlobalR2dbcFactory().create())
            } else {
                log.debug("Using TENANT R2DBC connection tenant={}", tenantId)
                Mono.from(
                    dataSourceManager
                        .getFactoryForTenant(tenantId)
                        .create()
                )
            }
        }

    override fun getMetadata(): ConnectionFactoryMetadata =
        dataSourceManager.getGlobalR2dbcFactory().metadata
}
