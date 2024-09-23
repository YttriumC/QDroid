package ng.i.sav.qdroid.log

import org.slf4j.helpers.ThreadLocalMapOfStacks
import org.slf4j.spi.MDCAdapter
import java.util.*

/*
* An MDC implement copied from qos-ch/logback
* */

/**
 * A *Mapped Diagnostic Context*, or MDC in short, is an instrument for
 * distinguishing interleaved log output from different sources. Log output is
 * typically interleaved when a server handles multiple clients
 * near-simultaneously.
 *
 *
 * ***The MDC is managed on a per-thread basis***. Note that a child
 * thread **does not** inherit the mapped diagnostic context of its parent.
 *
 *
 *
 *
 * For more information about MDC, please refer to the online manual at
 * http://logback.qos.ch/manual/mdc.html
 *
 * @author Ceki Glc
 * @author Michael Franz
 */
class DMCAdaptorImpl : MDCAdapter {
    // BEWARE: Keys or values placed in a ThreadLocal should not be of a type/class
    // not included in the JDK. See also https://jira.qos.ch/browse/LOGBACK-450
    private val readWriteThreadLocalMap = ThreadLocal<MutableMap<String, String>?>()
    private val readOnlyThreadLocalMap = ThreadLocal<Map<String, String>?>()
    private val threadLocalMapOfDeques = ThreadLocalMapOfStacks()

    /**
     * Put a context value (the `val` parameter) as identified with the
     * `key` parameter into the current thread's context map. Note that
     * contrary to log4j, the `val` parameter can be null.
     *
     *
     *
     *
     * If the current thread does not have a context map it is created as a side
     * effect of this call.
     *
     *
     *
     *
     * Each time a value is added, a new instance of the map is created. This is
     * to be certain that the serialization process will operate on the updated
     * map and not send a reference to the old map, thus not allowing the remote
     * logback component to see the latest changes.
     *
     * @throws IllegalArgumentException in case the "key" parameter is null
     */

    override fun put(key: String, `val`: String) {
        var current = readWriteThreadLocalMap.get()
        if (current == null) {
            current = HashMap()
            readWriteThreadLocalMap.set(current)
        }
        current[key] = `val`
        nullifyReadOnlyThreadLocalMap()
    }

    /**
     * Get the context identified by the `key` parameter.
     *
     *
     *
     *
     * This method has no side effects.
     */
    override fun get(key: String): String {
        val hashMap: Map<String, String>? = readWriteThreadLocalMap.get()
        return hashMap?.get(key) ?: ""
    }

    /**
     *
     * Remove the context identified by the `key` parameter.
     *
     *
     */
    override fun remove(key: String) {
        val current = readWriteThreadLocalMap.get()
        if (current != null) {
            current.remove(key)
            nullifyReadOnlyThreadLocalMap()
        }
    }

    private fun nullifyReadOnlyThreadLocalMap() {
        readOnlyThreadLocalMap.set(null)
    }

    /**
     * Clear all entries in the MDC.
     */
    override fun clear() {
        readWriteThreadLocalMap.set(null)
        nullifyReadOnlyThreadLocalMap()
    }

    val propertyMap: Map<String, String>?
        /**
         *
         * Get the current thread's MDC as a map. This method is intended to be used
         * internally.
         *
         * The returned map is unmodifiable (since version 1.3.2/1.4.2).
         */
        get() {
            var readOnlyMap = readOnlyThreadLocalMap.get()
            if (readOnlyMap == null) {
                val current: Map<String, String>? = readWriteThreadLocalMap.get()
                if (current != null) {
                    val tempMap: Map<String, String> = HashMap(current)
                    readOnlyMap = Collections.unmodifiableMap(tempMap)
                    readOnlyThreadLocalMap.set(readOnlyMap)
                }
            }
            return readOnlyMap
        }

    /**
     * Return a copy of the current thread's context map. Returned value may be
     * null.
     */
    override fun getCopyOfContextMap(): MutableMap<String, String>? {
        val readOnlyMap = propertyMap
        return if (readOnlyMap == null) {
            null
        } else {
            HashMap(readOnlyMap)
        }
    }

    val keys: Set<String>
        /**
         * Returns the keys in the MDC as a [Set]. The returned value can be
         * null.
         */
        get() {
            val readOnlyMap = propertyMap
            return readOnlyMap?.keys ?: HashSet()
        }

    override fun setContextMap(contextMap: MutableMap<String, String>?) {
        if (contextMap != null) {
            readWriteThreadLocalMap.set(HashMap<String, String>(contextMap))
        } else {
            readWriteThreadLocalMap.set(null)
        }
        nullifyReadOnlyThreadLocalMap()
    }

    override fun pushByKey(key: String, value: String) {
        threadLocalMapOfDeques.pushByKey(key, value)
    }

    override fun popByKey(key: String): String {
        return threadLocalMapOfDeques.popByKey(key)
    }

    override fun getCopyOfDequeByKey(key: String): Deque<String> {
        return threadLocalMapOfDeques.getCopyOfDequeByKey(key)
    }

    override fun clearDequeByKey(key: String) {
        threadLocalMapOfDeques.clearDequeByKey(key)
    }
}
