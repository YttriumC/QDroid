package ng.i.sav.qdroid.infra.annotation


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Order(
    val order: Int
) {
    companion object {
        fun sort(mutableCollection: Collection<Any>, desc: Boolean = false) {
            mutableCollection.sortedBy {
                if (it::class.java.isAnnotationPresent(Order::class.java)) {
                    if (desc)
                        -it::class.java.getAnnotation(Order::class.java).order
                    else
                        it::class.java.getAnnotation(Order::class.java).order
                } else {
                    if (desc)
                        Int.MIN_VALUE
                    else
                        Int.MAX_VALUE
                }
            }
        }
    }
}
