package io.iggdrasil.mtm.db.metrics

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.db.managers.mongo.TenantAwareReactiveMongoFactory
import io.iggdrasil.mtm.db.managers.postgres.TenantAwareConnectionFactory
import io.iggdrasil.mtm.models.MtMResourceReport
import io.iggdrasil.mtm.models.MtMStatusResponse
import io.iggdrasil.mtm.models.MtMTenantDetail
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MtMInspector(
    @Lazy private val r2dbc: TenantAwareConnectionFactory,
    @Lazy private val mongo: TenantAwareReactiveMongoFactory,
    private val props: MultiTenancyProperties
) {

    companion object {
        const val RESOURCE_SQL = "SQL_R2DBC"
        const val RESOURCE_MONGO = "NOSQL_MONGO"
    }

    fun getStatus(): MtMStatusResponse {
        val now = System.currentTimeMillis()
        val reports = mutableListOf<MtMResourceReport>()

        reports.add(
            MtMResourceReport(
                type = RESOURCE_SQL,
                activeTenants = r2dbc.activePools.size,
                details = r2dbc.activePools.map { (id, cached) ->
                    MtMTenantDetail(
                        tenantId = id,
                        idleMs = now - cached.lastAccess,
                        lastAccess = Instant.ofEpochMilli(cached.lastAccess)
                    )
                }
            )
        )

        reports.add(
            MtMResourceReport(
                type = RESOURCE_MONGO,
                activeTenants = mongo.activeClients.size,
                details = mongo.activeClients.map { (id, cached) ->
                    MtMTenantDetail(
                        tenantId = id,
                        idleMs = now - cached.lastAccess,
                        lastAccess = Instant.ofEpochMilli(cached.lastAccess)
                    )
                }
            )
        )

        return MtMStatusResponse(
            engine = props.dataSource.type.name,
            reports = reports
        )
    }
}