package ng.i.sav.qdroid.infra.annotation

import ng.i.sav.qdroid.infra.config.PersistenceBeanDefinitionRegistrarSupport
import ng.i.sav.qdroid.infra.config.QDroidConfiguration
import ng.i.sav.qdroid.infra.persistence.InMemoryPersistence
import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)

@Import(QDroidConfiguration::class, PersistenceBeanDefinitionRegistrarSupport::class)
annotation class EnableQDroid(
    val persistenceImplementation: KClass<*> = InMemoryPersistence::class
)
