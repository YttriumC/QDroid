package ng.i.sav.qdroid.infra.web

import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.DefaultEventExecutor
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.client.ReactorResourceFactory
import org.springframework.http.codec.support.DefaultServerCodecConfigurer
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.DispatcherHandler
import org.springframework.web.reactive.accept.HeaderContentTypeResolver
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler
import org.springframework.web.reactive.resource.PathResourceResolver
import org.springframework.web.reactive.resource.ResourceWebHandler
import org.springframework.web.reactive.result.SimpleHandlerAdapter
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.DisposableServer
import reactor.netty.http.HttpProtocol
import reactor.netty.http.server.HttpServer
import java.time.Duration


@Configuration
open class NettyServerInitializer(@Autowired(required = false) webServerConfigurer: WebServerConfigurer?) {
    private lateinit var webServerConfigurer: WebServerConfigurer

    init {
        if (webServerConfigurer == null)
            this.webServerConfigurer = WebServerConfigurer()
        log.info("use web server")
    }

    @Bean
    open fun webHandler(): DispatcherHandler {
        return DispatcherHandler()
    }

    @Bean
    open fun reactorResourceFactory(): ReactorResourceFactory {
        return ReactorResourceFactory()
    }

    @Bean
    open fun httpHandler(context: ApplicationContext): HttpHandler {
        return WebHttpHandlerBuilder.applicationContext(context).exceptionHandler(WebFluxResponseStatusExceptionHandler()).build()
    }

    @Bean
    open fun resourceWebHandler(): ResourceWebHandler {
        return ResourceWebHandler().apply {
            locations = listOf(ClassPathResource("static/"))
            resourceResolvers.add(PathResourceResolver().apply { setAllowedLocations(ClassPathResource("static/")) })
        }
    }

    @Bean
    open fun disposableServer(
        httpHandler: HttpHandler,
        reactorResourceFactory: ReactorResourceFactory,
    ): DisposableServer {
        return HttpServer.create().accessLog(true).bindAddress { webServerConfigurer.getBindSocketAddress() }
            .protocol(*listProtocols())
            .channelGroup(DefaultChannelGroup(DefaultEventExecutor())).handle(ReactorHttpHandlerAdapter(httpHandler))
            .runOn(reactorResourceFactory.loopResources).bindNow(Duration.ofSeconds(10))
    }

    @Bean
    open fun webFluxResponseStatusExceptionHandler(): WebFluxResponseStatusExceptionHandler {
        return WebFluxResponseStatusExceptionHandler()
    }

    @Bean
    open fun requestMappingHandlerMapping(): RequestMappingHandlerMapping {
        return RequestMappingHandlerMapping()
    }

    @Bean
    open fun simpleUrlHandlerMapping(): SimpleUrlHandlerMapping {
        return SimpleUrlHandlerMapping(hashMapOf("/static/**" to "resourceWebHandler"))
    }

    @Bean
    open fun simpleHandlerAdapter(): SimpleHandlerAdapter {
        return SimpleHandlerAdapter()
    }

    @Bean
    open fun routerFunctionMapping(): RouterFunctionMapping {
        return RouterFunctionMapping()
    }

    @Bean
    open fun requestMappingHandlerAdapter(defaultServerCodecConfigurer: DefaultServerCodecConfigurer): RequestMappingHandlerAdapter {
        return RequestMappingHandlerAdapter().apply { messageReaders = defaultServerCodecConfigurer.readers }
    }

    @Bean
    open fun responseBodyResultHandler(defaultServerCodecConfigurer: DefaultServerCodecConfigurer): ResponseBodyResultHandler {
        return ResponseBodyResultHandler(
            defaultServerCodecConfigurer.writers, HeaderContentTypeResolver()
        )
    }

    @Bean
    open fun defaultServerCodecConfigurer(): DefaultServerCodecConfigurer {
        return DefaultServerCodecConfigurer()
    }

    private fun listProtocols(): Array<HttpProtocol> {
        return arrayOf(HttpProtocol.HTTP11, HttpProtocol.H2C)
    }

    companion object {
        private val log = Slf4kt.getLogger(NettyServerInitializer::class.java)
    }
}
