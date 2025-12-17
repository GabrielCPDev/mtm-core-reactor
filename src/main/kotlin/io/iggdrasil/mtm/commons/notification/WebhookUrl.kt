package io.iggdrasil.mtm.commons.notification

@JvmInline
value class WebhookUrl private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Webhook URL cannot be empty" }
        require(value.startsWith("http://") || value.startsWith("https://")) {
            "Webhook URL must start with http:// or https://"
        }
        require(value.length <= 500) { "Webhook URL cannot exceed 500 characters" }
    }

    companion object {
        fun of(value: String): WebhookUrl = WebhookUrl(value.trim())
    }

    override fun toString(): String = value
}