package io.iggdrasil.mtm.db.managers

import io.iggdrasil.mtm.db.RepositoryScopeProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Proxy

@Component
class RepositoryInvocationInterceptor(
    private val scopeProcessor: RepositoryScopeProcessor
) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {

        val clazz = bean::class.java

        if (!clazz.interfaces.any { it.simpleName.endsWith("Repository") }) {
            return bean
        }

        val isGlobal = scopeProcessor.isGlobal(clazz)
        val isTenant = scopeProcessor.isTenant(clazz)

        if (!isGlobal && !isTenant) return bean

        return Proxy.newProxyInstance(
            clazz.classLoader,
            clazz.interfaces
        ) { _, method, args ->
            val result = method.invoke(bean, *(args ?: emptyArray()))

            when (result) {
                is Mono<*> ->
                    result.contextWrite { ctx ->
                        ctx.put("repo-scope", if (isGlobal) "GLOBAL" else "TENANT")
                    }

                is Flux<*> ->
                    result.contextWrite { ctx ->
                        ctx.put("repo-scope", if (isGlobal) "GLOBAL" else "TENANT")
                    }

                else ->
                    result
            }
        }
    }
}
