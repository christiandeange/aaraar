package sh.christian.aaraar.model.lib

sealed interface Value {
  data class Value32(val value: Int) : Value {
    override fun toString(): String = value.toString()
  }

  data class Value64(val value: Long) : Value {
    override fun toString(): String = value.toString()
  }

  val isZero: Boolean
    get() = when (this) {
      is Value32 -> value == 0
      is Value64 -> value == 0L
    }
}
