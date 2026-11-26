package io.iggdrasil.testapp.mongo.main

import io.iggdrasil.testapp.mongo.TestDocument
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

/** Pacote listado em `mtm.repositories.main`. */
interface MainDocumentRepository : ReactiveMongoRepository<TestDocument, String>
