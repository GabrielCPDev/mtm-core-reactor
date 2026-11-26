package io.iggdrasil.mtm.db.managers

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource
import org.springframework.data.repository.config.RepositoryConfigurationDelegate
import org.springframework.data.repository.config.RepositoryConfigurationExtension
import org.springframework.data.util.Streamable

/**
 * Registra os repositórios Spring Data de UM escopo (`main` ou `tenant`), lendo os pacotes
 * de `mtm.repositories.<escopo>` (uma lista no yaml) e ligando-os ao template do escopo.
 *
 * Faz o que um `@Enable...Repositories(basePackages = ...)` faria, mas com os pacotes vindos
 * da configuração em tempo de boot — a anotação não aceita uma lista do yaml. As opções da
 * anotação (template etc.) vêm de [settings], uma classe que só carrega a anotação
 * [enableAnnotation] e nunca é registrada como bean.
 */
abstract class ScopedRepositoriesRegistrar(
    private val scope: String,
    private val settings: Class<*>,
    private val enableAnnotation: Class<out Annotation>,
) : ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private lateinit var environment: Environment
    private lateinit var resourceLoader: ResourceLoader

    /** A extensão do Spring Data do banco (Mongo reativo ou R2DBC). */
    protected abstract fun extension(): RepositoryConfigurationExtension

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        importBeanNameGenerator: BeanNameGenerator,
    ) {
        val packages = Binder.get(environment)
            .bind("mtm.repositories.$scope", Bindable.listOf(String::class.java))
            .orElse(emptyList())
            .orEmpty()
            .filter { it.isNotBlank() }
        if (packages.isEmpty()) return

        val source = object : AnnotationRepositoryConfigurationSource(
            AnnotationMetadata.introspect(settings),
            enableAnnotation,
            resourceLoader,
            environment,
            registry,
            importBeanNameGenerator,
        ) {
            override fun getBasePackages(): Streamable<String> = Streamable.of(packages)
        }
        RepositoryConfigurationDelegate(source, resourceLoader, environment)
            .registerRepositoriesIn(registry, extension())
    }
}
