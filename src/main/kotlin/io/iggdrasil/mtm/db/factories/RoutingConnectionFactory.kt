package io.iggdrasil.mtm.db.factories

import io.iggdrasil.mtm.db.managers.DataSourceManagerR2dbc
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RoutingConnectionFactory(
    private val dataSourceManager: DataSourceManagerR2dbc
) : ConnectionFactory {

    override fun create(): Mono<out Connection> {
        return Mono.deferContextual { ctx ->
            val scope = ctx.getOrDefault("repo-scope", "TENANT") as String

            mono {
                val factory =
                    if (scope == "GLOBAL")
                        dataSourceManager.getGlobalR2dbcFactory()
                    else
                        dataSourceManager.getCurrentR2dbcFactory()

                Mono.from(factory.create()).awaitSingle()
            }
        }
    }

    override fun getMetadata(): ConnectionFactoryMetadata =
        dataSourceManager.getGlobalR2dbcFactory().metadata
}