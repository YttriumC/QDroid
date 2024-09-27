package ng.i.sav.qdroid.infra.config

import ng.i.sav.qdroid.infra.annotation.EnableQDroid
import ng.i.sav.qdroid.infra.client.Persistence
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration


//@Configuration
open class PersistenceConfiguration : BeanFactoryAware {

    companion object {
        private val log = Slf4kt.getLogger(PersistenceConfiguration::class.java)
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        log.info("Obtain BeanFactory")
        beanFactory as ConfigurableListableBeanFactory
        val beanNames = beanFactory.getBeanNamesForAnnotation(EnableQDroid::class.java)
        beanNames.forEach {
            val configurer = beanFactory.getBean(it)
            val annotationsByType = configurer.javaClass.getAnnotationsByType(EnableQDroid::class.java)
            annotationsByType.forEach { annotation ->
                when (val impl = annotation.persistenceImplementation) {
                    Persistence::class -> {
                        log.error("Persistence model not properly configured: {}", impl)
                        throw IllegalArgumentException("$impl is not a concrete type.")
                    }

                    else -> {
                        if (impl.isAbstract) {
                            log.error("{} is not a concrete type, can not be instantiated", impl)
                            throw IllegalArgumentException("$impl is not a concrete type.")
                        } else {
                            if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, impl.java).isEmpty()) {
                                val autowired = beanFactory.autowire(
                                    impl.java,
                                    AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR,
                                    true
                                )
                                beanFactory.registerSingleton(impl.java.name, autowired)
                            }
                        }
                    }
                }
            }
        }
    }
}
