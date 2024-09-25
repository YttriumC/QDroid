package ng.i.sav.qdroid.cutom

import ng.i.sav.qdroid.bot.event.Status
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@ComponentScan
@Configuration
@Import(Status::class)
open class CustomConfig