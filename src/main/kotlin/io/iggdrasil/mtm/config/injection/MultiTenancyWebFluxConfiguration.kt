package io.iggdrasil.mtm.config.injection

import io.iggdrasil.mtm.config.props.MultiTenancyProperties
import io.iggdrasil.mtm.config.setup.CustomTenantResolver
import io.iggdrasil.mtm.tenant.TenantContextHolder
import io.iggdrasil.mtm.tenant.TenantResolver
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.web.server.WebFilter

@AutoConfiguration
@ConditionalOnClass(WebFilter::class)
class MultiTenancyWebFluxConfiguration {

    @Bean
    fun tenantWebFilter(
        properties: MultiTenancyProperties,
        context: TenantContextHolder,
        customTenantResolver: CustomTenantResolver?
    ): WebFilter =
        TenantResolver(properties, context, customTenantResolver)
}