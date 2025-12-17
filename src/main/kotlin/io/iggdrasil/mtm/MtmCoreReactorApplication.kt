package io.iggdrasil.mtm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MtmCoreReactorApplication

fun main(args: Array<String>) {
	runApplication<MtmCoreReactorApplication>(*args)
}
