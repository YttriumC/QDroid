import ng.i.sav.qdroid.infra.client.BotEventDispatcher
import ng.i.sav.qdroid.infra.client.BotManager
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import ng.i.sav.qdroid.client.event.Echo
import ng.i.sav.qdroid.client.lifecycle.DefaultLifecycle
import ng.i.sav.qdroid.log.Slf4kt
import org.slf4j.event.Level
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess

fun main() {

    try {
        Properties().apply {
            load(File("botApi.properties").reader())
        }.let {
            val token = it["token"] as String
            val appId = it["appId"] as String
            val objectMapper = ObjectMapper().apply {
                this.registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
            Slf4kt.setLevel(Level.DEBUG)
            val converter = MappingJackson2HttpMessageConverter(objectMapper)
            val botManager = BotManager(
                appId,
                token,
                "https://sandbox.api.sgroup.qq.com",
//            "https://api.sgroup.qq.com/",
                RestClient(RestTemplate(listOf(converter))),
                WsClient(StandardWebSocketClient()),
                HttpRequestPool().apply { init() },
                Intents.allPublicMessage(),
                objectMapper,
                arrayListOf(DefaultLifecycle()),
                BotEventDispatcher(
                    arrayListOf(Echo()), objectMapper
                ),
                1
            )
            botManager.startAsync()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(-1)
    }
}
