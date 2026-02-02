package io.iggdrasil.mtm.db.providers.metrics

interface ProviderStatus {
    fun status(): ProviderStatusInfo
}
