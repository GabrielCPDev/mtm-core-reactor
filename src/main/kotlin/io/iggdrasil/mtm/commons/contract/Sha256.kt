package io.iggdrasil.mtm.commons.contract

import java.security.MessageDigest

@JvmInline
value class Sha256 private constructor(val value: String) {
    init {
        require(value.matches(Regex("^[a-f0-9]{64}$"))) { "Invalid SHA-256" }
    }

    companion object {
        fun of(hex: String) = Sha256(hex.lowercase())

        fun generate(data: ByteArray): Sha256 {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(data)
            val hex = hash.joinToString("") { "%02x".format(it) }
            return Sha256(hex)
        }
    }

    override fun toString() = value
}