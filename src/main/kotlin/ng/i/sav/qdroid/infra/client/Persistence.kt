package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

interface Persistence {
    suspend fun save(key: String, value: Any?)
    suspend fun <T> get(key: String, clazz: Class<T>): T?
    suspend fun <T> get(key: String, typeReference: TypeReference<T>): T?
    suspend fun delete(key: String)
    fun checkKey(key: String): Boolean {
        return StringUtils.hasText(key)
    }
}
