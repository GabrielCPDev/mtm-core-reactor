package io.iggdrasil.mtm.config.props

import io.iggdrasil.mtm.tenant.TenantResolverType

data class TenantProperties(
    var resolverType: TenantResolverType = TenantResolverType.HEADER,
    var headerName: String = "X-Tenant-ID",
    var paramName: String = "tenantId"
)