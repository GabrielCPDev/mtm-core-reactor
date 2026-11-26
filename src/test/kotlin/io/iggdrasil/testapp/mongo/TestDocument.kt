package io.iggdrasil.testapp.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "test_documents")
data class TestDocument(@Id val id: String, val description: String)
