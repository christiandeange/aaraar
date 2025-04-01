package sh.christian.aaraar.model.lib

enum class NativeEndian {
  LITTLE,
  BIG,
  ;

  val value: Byte
    get() = when (this) {
      LITTLE -> 0x01
      BIG -> 0x02
    }

  companion object {
    fun from(value: Byte): NativeEndian = when (value) {
      LITTLE.value -> LITTLE
      BIG.value -> BIG
      else -> throw IllegalArgumentException("Invalid endianness: $value")
    }
  }
}
