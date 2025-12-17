package io.iggdrasil.mtm.commons.user.plan

enum class PlanType(
    val maxClients: Int,
    val maxInstancesPerClient: Int
) {
    FREE(
        maxClients = 3,
        maxInstancesPerClient = 5
    ),
    PRO(
        maxClients = 10,
        maxInstancesPerClient = 50
    ),
    ENTERPRISE(
        maxClients = 100,
        maxInstancesPerClient = 500
    ),
    UNLIMITED(
        maxClients = 999999999,
        maxInstancesPerClient = 999999999
    )
}