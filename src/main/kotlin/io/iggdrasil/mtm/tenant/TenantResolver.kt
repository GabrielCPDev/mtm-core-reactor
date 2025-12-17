package io.iggdrasil.mtm.tenant

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.setup.CustomTenantResolver
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class TenantResolver(
    private val properties: MultiTenancyProperties,
    private val tenantContext: TenantContextHolder,
    private val customTenantResolver: CustomTenantResolver? = null
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val tenantId = resolveTenantId(exchange)

        return if (tenantId != null) {
            chain.filter(exchange)
                .contextWrite(tenantContext.contextWriter(tenantId))
        } else {
            chain.filter(exchange)
        }
    }

    private fun resolveTenantId(exchange: ServerWebExchange): String? =
        when (properties.tenant.resolverType) {
            TenantResolverType.HEADER ->
                exchange.request.headers.getFirst(properties.tenant.headerName)

            TenantResolverType.PARAM ->
                exchange.request.queryParams.getFirst(properties.tenant.paramName)

            TenantResolverType.CUSTOM ->
                customTenantResolver?.resolve(exchange)
        }
}
