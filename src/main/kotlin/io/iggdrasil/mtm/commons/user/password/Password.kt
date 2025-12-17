package io.iggdrasil.mtm.commons.user.password

@JvmInline
value class Password private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Password cannot be blank" }
    }

    companion object {
        fun ofRaw(rawPassword: String): Password {
            require(rawPassword.length >= 8) {
                "Password must be at least 8 characters"
            }
            require(rawPassword.any { it.isDigit() }) {
                "Password must contain at least one digit"
            }

            return Password(rawPassword)
        }

        fun ofHashed(hashedPassword: String): Password {
            return Password(hashedPassword)
        }
    }
}