package sh.christian.aaraar.model.lib

sealed interface Value {
  data class Value32(val value: Int) : Value {
    override fun toString(): String = value.toString()
  }

  data class Value64(val value: Long) : Value {
    override fun toString(): String = value.toString()
  }
}
