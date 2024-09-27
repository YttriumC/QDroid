package ng.i.sav.qdroid.infra.persistence

import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.i.sav.qdroid.infra.client.Persistence
import ng.i.sav.qdroid.infra.config.ComponentConfiguration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component


@Component
class H2Persistence(
    componentConfiguration: ComponentConfiguration,
    private val extensionsRepo: ExtensionsRepo
) : Persistence {
    private val objectMapper = componentConfiguration.getObjectMapper()
    override suspend fun save(key: String, value: Any?) {
        withContext(Dispatchers.IO) {
            if (value == null)
                return@withContext extensionsRepo.deleteById(key)
            extensionsRepo.save(Extensions.with(key, objectMapper.writeValueAsString(value)))
        }
    }

    override suspend fun <T> get(key: String, clazz: Class<T>): T? {
        return withContext(Dispatchers.IO) {
            extensionsRepo.findByIdOrNull(key)?.let {
                objectMapper.readValue(it.value, clazz)
            }
        }
    }

    override suspend fun <T> get(key: String, typeReference: TypeReference<T>): T? {
        return withContext(Dispatchers.IO) {
            extensionsRepo.findByIdOrNull(key)?.let {
                objectMapper.readValue(it.value, typeReference)
            }
        }
    }

    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            extensionsRepo.deleteById(key)
        }
    }

}
