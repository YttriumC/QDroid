package ng.i.sav.qdroid.infra.config

import ng.i.sav.qdroid.infra.annotation.EnableQDroid
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata

class PersistenceBeanDefinitionRegistrarSupport : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry, importBeanNameGenerator: BeanNameGenerator
    ) {
        registerBeanDefinition(importingClassMetadata, registry, importBeanNameGenerator)
    }

    private fun registerBeanDefinition(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        importBeanNameGenerator: BeanNameGenerator?
    ) {

        importingClassMetadata.annotations[EnableQDroid::class.java]
            .asAnnotationAttributes()["persistenceImplementation"].also { c ->
            BeanDefinitionBuilder.genericBeanDefinition(c as Class<*>)
                .setScope(ConfigurableBeanFactory.SCOPE_SINGLETON).beanDefinition.also {
                    registry.registerBeanDefinition(
                        importBeanNameGenerator?.generateBeanName(it, registry) ?: c.name, it
                    )
                }
        }
    }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        registerBeanDefinition(importingClassMetadata, registry, null)
    }
}
