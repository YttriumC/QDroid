import kotlinx.coroutines.runBlocking
import ng.i.sav.qdroid.infra.annotation.EnableQDroid
import ng.i.sav.qdroid.infra.annotation.UseWeb
import ng.i.sav.qdroid.infra.client.BotManager
import ng.i.sav.qdroid.infra.persistence.H2Configuration
import ng.i.sav.qdroid.infra.web.NettyServerInitializer
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import reactor.netty.http.HttpProtocol


@EnableQDroid(H2Configuration::class)
@UseWeb
open class Starter {
    fun initSpring(vararg components: Class<*>) = runBlocking {
        log.info("__qdroid__")
        val applicationContext = AnnotationConfigWebApplicationContext()
        applicationContext.register(NettyServerInitializer::class.java, Starter::class.java, *components)
//        applicationContext.refresh()
        /*   val handler = ReactorHttpHandlerAdapter(HttpWebHandlerAdapter(DispatcherHandler(applicationContext)))
           val disposableServer = HttpServer.create().accessLog(true)
               .bindAddress { java.net.InetSocketAddress(18089) }
               .channelGroup(DefaultChannelGroup(DefaultEventExecutor()))
               .handle(ReactorHttpHandlerAdapter(HttpWebHandlerAdapter(ResourceWebHandler().apply {
                   setLocations(listOf(ClassPathResource("static")))
               })))
               .handle(handler).protocol(*listProtocols())
               .runOn(ReactorResourceFactory().loopResources).bindNow(Duration.ofSeconds(10))*/
        applicationContext.refresh()
        val bot = applicationContext.getBean(BotManager::class.java)
        bot.startAsync()
        applicationContext.registerShutdownHook()
        Runtime.getRuntime().addShutdownHook(Thread {
            bot.shutdown()
        })

    }

    private fun listProtocols(): Array<HttpProtocol> {
        return arrayOf(HttpProtocol.HTTP11, HttpProtocol.H2C)
    }

    companion object {
        private val log = Slf4kt.getLogger(Starter::class.java)
    }
}
