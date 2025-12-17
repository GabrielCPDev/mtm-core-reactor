package io.iggdrasil.mtm.db

import io.iggdrasil.mtm.db.annotations.GlobalRepository
import io.iggdrasil.mtm.db.annotations.MultiTenantRepository
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RepositoryScopeProcessor : BeanPostProcessor {

    private val globalRepos = ConcurrentHashMap.newKeySet<Class<*>>()
    private val tenantRepos = ConcurrentHashMap.newKeySet<Class<*>>()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val clazz = bean::class.java

        when {
            clazz.isAnnotationPresent(GlobalRepository::class.java) ->
                globalRepos.add(clazz)

            clazz.isAnnotationPresent(MultiTenantRepository::class.java) ->
                tenantRepos.add(clazz)
        }

        return bean
    }

    fun isGlobal(clazz: Class<*>): Boolean =
        globalRepos.contains(clazz)

    fun isTenant(clazz: Class<*>): Boolean =
        tenantRepos.contains(clazz)
}