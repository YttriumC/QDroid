package ng.i.sav.qdroid.infra.persistence

import com.fasterxml.jackson.core.type.TypeReference
import ng.i.sav.qdroid.infra.client.Persistence
import ng.i.sav.qdroid.infra.config.ComponentConfiguration

class InMemoryPersistence(componentConfiguration: ComponentConfiguration) : Persistence {
    private val objectMapper = componentConfiguration.getObjectMapper()
    private val saver = hashMapOf<String, String>()
    override suspend fun save(key: String, value: Any?) {
        if (checkKey(key).not()) {
            throw IllegalArgumentException("key must not be empty")
        }
        value?.let {
            /* As same as H2 behavior */
            saver[key] = objectMapper.writeValueAsString(value)
        }
    }

    override suspend fun <T> get(key: String, clazz: Class<T>): T? {
        return saver[key]?.let { objectMapper.readValue(it, clazz) }
    }

    override suspend fun <T> get(key: String, typeReference: TypeReference<T>): T? {
        return saver[key]?.let { objectMapper.readValue(it, typeReference) }
    }

    override suspend fun delete(key: String) {
        saver.remove(key)
    }

}
