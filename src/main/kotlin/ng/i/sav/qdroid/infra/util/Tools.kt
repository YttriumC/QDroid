package ng.i.sav.qdroid.infra.util

object Tools {
    fun anyNotNull(vararg any: Any?): Boolean {
        return any.any { it != null }
    }

    fun allNull(vararg any: Any?): Boolean {
        return any.all { it == null }
    }

    fun anyTrue(vararg bool: Boolean): Boolean {
        return bool.any { it }
    }

    fun allTrue(vararg bool: Boolean): Boolean {
        return !bool.any { !it }
    }

    fun allFalse(vararg bool: Boolean): Boolean {
        return !bool.any { it }
    }

    fun anyNotBlank(vararg charSequence: CharSequence?): Boolean {
        return charSequence.any { it?.isNotBlank() ?: false }
    }

    fun anyBlank(vararg charSequence: CharSequence?): Boolean {
        return charSequence.any { it?.isBlank() ?: true }
    }

    fun allNotBlank(vararg charSequence: CharSequence?): Boolean {
        return !charSequence.any { it?.isBlank() ?: true }
    }

    fun allBlank(vararg charSequence: CharSequence?): Boolean {
        return charSequence.all { it?.isBlank() ?: true }
    }
}

