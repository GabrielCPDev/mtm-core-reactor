package io.iggdrasil.mtm.commons.tenant

@JvmInline
value class Metadata(val values: Map<String, String>) {

    init {
        require(values.size <= 50) { "Metadata cannot have more than 50 entries" }
    }

    companion object {
        fun empty(): Metadata = Metadata(emptyMap())
        fun of(vararg pairs: Pair<String, String>): Metadata = Metadata(mapOf(*pairs))
    }

    operator fun get(key: String): String? = values[key]

    fun put(key: String, value: String): Metadata = Metadata(values + (key to value))
}