package ng.i.sav.qdroid.infra.persistence

import jakarta.persistence.*

@Entity
@Table(name = "extensions")
class Extensions() {
    constructor(key: String, value: String) : this() {
        this.key = key
        this.value = value
    }

    @Id
    @Column(name = "`key`", nullable = false, length = 128)
    var key: String = ""

    @Lob
    @Column(name = "`value`", nullable = false)
    var value: String = ""

    override fun toString(): String {
        return "Property(name='$key', value='$value')"
    }

    companion object {
        fun with(key: String, value: String) = Extensions(key, value)
    }
}
