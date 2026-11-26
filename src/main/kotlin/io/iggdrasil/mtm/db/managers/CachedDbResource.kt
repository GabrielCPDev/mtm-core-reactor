package io.iggdrasil.mtm.db.managers

data class CachedDbResource<T>(
    val resource: T,
    @Volatile var lastAccess: Long = System.currentTimeMillis()
) {
    fun touch() {
        lastAccess = System.currentTimeMillis()
    }

    fun isExpired(timeoutMillis: Long): Boolean {
        return System.currentTimeMillis() - lastAccess > timeoutMillis
    }
}