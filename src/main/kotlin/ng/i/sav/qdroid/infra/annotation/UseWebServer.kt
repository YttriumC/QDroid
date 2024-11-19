package ng.i.sav.qdroid.infra.annotation

import ng.i.sav.qdroid.infra.web.NettyServerInitializer
import org.springframework.context.annotation.Import

@Import(NettyServerInitializer::class)
annotation class UseWebServer
