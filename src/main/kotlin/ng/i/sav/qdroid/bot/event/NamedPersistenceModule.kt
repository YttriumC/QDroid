package ng.i.sav.qdroid.bot.event

import com.fasterxml.jackson.core.type.TypeReference
import ng.i.sav.qdroid.infra.client.Persistence
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class NamedPersistenceModule(
    private val persistence: Persistence,
) {

    fun getPersistence(named: Named): Persistence {
        if (StringUtils.hasText(named.getDomain())) {
            return (named.getDomain().trimEnd('.') + '.').let {
                object : Persistence {
                    override suspend fun save(key: String, value: Any?) {
                        return persistence.save("$it$key", value)
                    }

                    override suspend fun <T> get(key: String, clazz: Class<T>): T? {
                        return persistence.get("$it$key", clazz)
                    }

                    override suspend fun <T> get(key: String, typeReference: TypeReference<T>): T? {
                        return persistence.get("$it$key", typeReference)
                    }

                    override suspend fun delete(key: String) {
                        return persistence.delete("$it$key")
                    }

                }
            }
        }
        throw IllegalArgumentException("domain must not be empty")
    }

}

interface Named {
    fun getDomain(): String
}
