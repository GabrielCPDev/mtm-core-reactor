package io.iggdrasil.mtm.tenant

import reactor.core.publisher.Mono
import reactor.util.context.Context

object TenantContext {
    const val TENANT_ID = "tenant-id"
    const val REPO_SCOPE = "repo-scope"

    fun write(ctx: Context, tenantId: String): Context =
        ctx.put(TENANT_ID, tenantId)

    fun read(): Mono<String> =
        Mono.deferContextual { ctx ->
            Mono.justOrEmpty(ctx.getOrEmpty<String>(TENANT_ID))
        }
}
