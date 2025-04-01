package sh.christian.aaraar.model.lib

enum class NativeFormat {
  BIT_32,
  BIT_64,
  ;

  val value: Byte
    get() = when (this) {
      BIT_32 -> 0x01
      BIT_64 -> 0x02
    }

  companion object {
    fun from(value: Byte): NativeFormat = when (value) {
      BIT_32.value -> BIT_32
      BIT_64.value -> BIT_64
      else -> throw IllegalArgumentException("Invalid address class: $value")
    }
  }
}
