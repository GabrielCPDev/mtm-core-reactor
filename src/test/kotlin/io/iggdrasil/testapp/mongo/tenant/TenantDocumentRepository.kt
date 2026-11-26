package io.iggdrasil.testapp.mongo.tenant

import io.iggdrasil.testapp.mongo.TestDocument
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

/** Pacote listado em `mtm.repositories.tenant`. */
interface TenantDocumentRepository : ReactiveMongoRepository<TestDocument, String>
