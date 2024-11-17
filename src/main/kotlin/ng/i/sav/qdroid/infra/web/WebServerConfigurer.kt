package ng.i.sav.qdroid.infra.web

import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
open class WebServerConfigurer {
    open fun getBindSocketAddress(): InetSocketAddress {
        return InetSocketAddress(18081)
    }
}
