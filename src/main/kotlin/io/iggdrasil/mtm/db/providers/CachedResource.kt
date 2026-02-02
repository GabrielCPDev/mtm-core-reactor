package io.iggdrasil.mtm.db.providers

data class CachedResource<T>(
    val resource: T,
    val cleanup: (T) -> Unit,
    @Volatile var lastAccess: Long = System.currentTimeMillis()
)
