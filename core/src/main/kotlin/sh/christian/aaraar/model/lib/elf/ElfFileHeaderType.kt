package sh.christian.aaraar.model.lib.elf

sealed class ElfFileHeaderType(val value: Short) {
  object None : ElfFileHeaderType(0x0000.toShort())
  object Rel : ElfFileHeaderType(0x0001.toShort())
  object Exec : ElfFileHeaderType(0x0002.toShort())
  object Dyn : ElfFileHeaderType(0x0003.toShort())
  object Core : ElfFileHeaderType(0x0004.toShort())
  class Other(value: Short) : ElfFileHeaderType(value)

  override fun toString(): String {
    return "0x${value.toString(16).padStart(4, '0')}"
  }

  override fun equals(other: Any?): Boolean {
    return value == (other as? ElfFileHeaderType)?.value
  }

  override fun hashCode(): Int {
    return value.toInt()
  }

  companion object {
    fun from(value: Short): ElfFileHeaderType = when (value) {
      0x0000.toShort() -> None
      0x0001.toShort() -> Rel
      0x0002.toShort() -> Exec
      0x0003.toShort() -> Dyn
      0x0004.toShort() -> Core
      else -> Other(value)
    }
  }
}
