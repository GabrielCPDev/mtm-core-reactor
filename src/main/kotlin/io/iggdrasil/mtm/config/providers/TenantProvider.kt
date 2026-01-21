package io.iggdrasil.mtm.config.providers

import io.iggdrasil.mtm.commons.tenant.Tenant

@Deprecated(
    message = "TenantProvider is no longer required for static data source configurations. Metadata is now resolved via MultiTenancyProperties.",
    replaceWith = ReplaceWith("MultiTenancyProperties")
)
interface TenantProvider {
    suspend fun getTenantById(tenantId: String): Tenant?
}