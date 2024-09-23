package ng.i.sav.qdroid.infra.config

import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.stereotype.Component

@Configurable(autowire = Autowire.BY_TYPE)
@Component
open class BotConfig() {

}
