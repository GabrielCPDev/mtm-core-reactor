package io.iggdrasil.mtm.config.setup

import org.springframework.web.server.ServerWebExchange

interface CustomTenantResolver {
    fun resolve(exchange: ServerWebExchange): String?
}