package io.iggdrasil.mtm.config.providers

import io.iggdrasil.mtm.commons.tenant.Tenant

interface TenantProvider {
    suspend fun getTenantById(tenantId: String): Tenant?
}