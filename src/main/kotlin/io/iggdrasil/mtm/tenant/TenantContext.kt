package io.iggdrasil.mtm.tenant

import reactor.core.publisher.Mono
import reactor.util.context.Context
import reactor.util.context.ContextView

object TenantContext {

    const val TENANT_ID = "tenant-id"

    fun write(ctx: Context, tenantId: String): Context =
        ctx.put(TENANT_ID, tenantId)

    fun write(tenantId: String): (Context) -> Context =
        { ctx -> ctx.put(TENANT_ID, tenantId) }

    fun read(): Mono<String> =
        Mono.deferContextual { ctx ->
            Mono.justOrEmpty(ctx.getOrEmpty<String>(TENANT_ID))
        }

    fun read(ctx: ContextView): String? =
        ctx.getOrEmpty<String>(TENANT_ID).orElse(null)

    fun require(ctx: ContextView): String =
        ctx.getOrEmpty<String>(TENANT_ID)
            .orElseThrow { IllegalStateException("Tenant not present in Reactor Context") }
}
