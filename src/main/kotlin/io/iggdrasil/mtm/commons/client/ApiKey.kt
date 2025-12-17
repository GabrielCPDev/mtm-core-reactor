package io.iggdrasil.mtm.commons.client

import java.util.UUID

@JvmInline
value class ApiKey private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "API Key cannot be empty" }
        require(value.startsWith("sk_")) { "API Key must start with 'sk_'" }
        require(value.length >= 32) { "API Key must have at least 32 characters" }
    }

    companion object {
        fun of(value: String): ApiKey = ApiKey(value)

        fun generate(): ApiKey {
            val randomKey = UUID.randomUUID().toString().replace("-", "")
            return ApiKey("sk_live_$randomKey")
        }
    }

    fun mask(): String = "${value.take(10)}...${value.takeLast(4)}"

    override fun toString(): String = mask()
}