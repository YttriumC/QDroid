import com.fasterxml.jackson.databind.ObjectMapper
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.util.extractSingleObject
import ng.i.sav.qdroid.infra.util.typeRef
import java.io.File

fun main() {
    println(
        ObjectMapper().readValue(
            File("D:\\DevelopmentWorkspace\\idea\\QDroid\\src\\test\\resources\\static\\t.json"),
            (typeRef<HashMap<String,Message>>())
        ).extractSingleObject<Message>()
    )
}
/* TODO
* Exception in thread "main" com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type
* `java.time.LocalDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to
*  enable handling at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 7, column: 18] (through reference
 * chain: java.util.HashMap["message"]->ng.i.sav.qdroid.infra.model.Message["timestamp"])
* */
