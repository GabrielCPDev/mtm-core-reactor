package io.iggdrasil.mtm.commons.contract

import models.Name

data class ContractFile(
    val fileName: Name,
    val contentType: String,
    val fileSize: Long,
    val checksum: Sha256,
    val data: ByteArray
) {
    init {
        require(fileName.value.isNotBlank()) { "fileName cannot be blank" }
        require(contentType == "application/pdf") { "Only PDF is allowed" }
        require(fileSize in 1..(10L * 1024 * 1024)) { "File must be between 1B and 10MB" }
        require(data.isNotEmpty()) { "data cannot be empty" }
        require(data.size.toLong() == fileSize) { "data size must match fileSize" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContractFile

        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false
        if (fileSize != other.fileSize) return false
        if (checksum != other.checksum) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + checksum.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}