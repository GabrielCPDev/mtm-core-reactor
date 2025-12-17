package io.iggdrasil.mtm.tenant

import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.Optional

class TenantContextHolder {

    companion object {
        private const val CONTEXT_KEY = "TENANT_ID"
    }

    fun getReactive(): Mono<String> =
        Mono.deferContextual { ctx ->
            val tenantId: Optional<String> = ctx.getOrEmpty(CONTEXT_KEY)

            Mono.justOrEmpty(tenantId)
        }

    fun contextWriter(tenantId: String): (Context) -> Context =
        { ctx -> ctx.put(CONTEXT_KEY, tenantId) }
}