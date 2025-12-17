package io.iggdrasil.mtm.commons.tenantEvent

enum class EventType {
    TENANT_CREATED,
    TENANT_EXPIRED,
    TENANT_DELETED,
    TENANT_DISABLED,
    CLIENT_EXPIRED,
    CLIENT_LIMIT_EXCEEDED
}