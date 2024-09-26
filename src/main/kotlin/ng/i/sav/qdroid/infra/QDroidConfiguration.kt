package ng.i.sav.qdroid.infra

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Import(QDroidConfiguration::class)
annotation class EnableQDroid

@Configuration
@ComponentScan
open class QDroidConfiguration
