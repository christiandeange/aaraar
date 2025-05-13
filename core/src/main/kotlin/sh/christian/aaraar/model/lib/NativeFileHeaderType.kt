package sh.christian.aaraar.model.lib

sealed class NativeFileHeaderType(val value: Short) {
  object None : NativeFileHeaderType(0x0000.toShort())
  object Rel : NativeFileHeaderType(0x0001.toShort())
  object Exec : NativeFileHeaderType(0x0002.toShort())
  object Dyn : NativeFileHeaderType(0x0003.toShort())
  object Core : NativeFileHeaderType(0x0004.toShort())
  class Other(value: Short) : NativeFileHeaderType(value)

  override fun toString(): String {
    return "0x${value.toString(16).padStart(Short.SIZE_BYTES * 2, '0')}"
  }

  override fun equals(other: Any?): Boolean {
    return value == (other as? NativeFileHeaderType)?.value
  }

  override fun hashCode(): Int {
    return value.toInt()
  }

  companion object {
    fun from(value: Short): NativeFileHeaderType = when (value) {
      0x0000.toShort() -> None
      0x0001.toShort() -> Rel
      0x0002.toShort() -> Exec
      0x0003.toShort() -> Dyn
      0x0004.toShort() -> Core
      else -> Other(value)
    }
  }
}
