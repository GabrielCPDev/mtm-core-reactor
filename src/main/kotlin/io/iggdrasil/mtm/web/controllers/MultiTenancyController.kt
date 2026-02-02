package io.iggdrasil.mtm.web.controllers

import io.iggdrasil.mtm.config.providers.ConnectionProvider
import io.iggdrasil.mtm.db.providers.metrics.ProviderStatus
import io.iggdrasil.mtm.db.providers.metrics.ProviderStatusInfo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mtm")
class MultiTenancyController(
    private val connectionProvider: ConnectionProvider<*>
) {

    @GetMapping("/status")
    fun status(): ProviderStatusInfo {
        val provider = connectionProvider as ProviderStatus
        return provider.status()
    }
}
