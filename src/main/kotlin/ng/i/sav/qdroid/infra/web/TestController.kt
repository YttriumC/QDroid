package ng.i.sav.qdroid.infra.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/test")
class TestController {

    @RequestMapping("/hello")
    fun hello(@RequestBody hashMap: HashMap<String,Any>): Mono<HashMap<String, Any>> {
        return Mono.just(hashMap)
    }
}
