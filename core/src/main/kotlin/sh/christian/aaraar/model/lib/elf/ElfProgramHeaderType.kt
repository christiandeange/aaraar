package sh.christian.aaraar.model.lib.elf

sealed class ElfProgramHeaderType(val value: Int) {
  object Null : ElfProgramHeaderType(0x00000000)
  object Load : ElfProgramHeaderType(0x00000001)
  object Dynamic : ElfProgramHeaderType(0x00000002)
  object Interp : ElfProgramHeaderType(0x00000003)
  object Note : ElfProgramHeaderType(0x00000004)
  object Shlib : ElfProgramHeaderType(0x00000005)
  object Phdr : ElfProgramHeaderType(0x00000006)
  object Tls : ElfProgramHeaderType(0x00000007)

  class Other(value: Int) : ElfProgramHeaderType(value)

  override fun toString(): String {
    return "0x${value.toString(16).padStart(8, '0')}"
  }

  override fun equals(other: Any?): Boolean {
    return value == (other as? ElfProgramHeaderType)?.value
  }

  override fun hashCode(): Int {
    return value
  }

  companion object {
    fun from(value: Int): ElfProgramHeaderType = when (value) {
      0x00000000 -> Null
      0x00000001 -> Load
      0x00000002 -> Dynamic
      0x00000003 -> Interp
      0x00000004 -> Note
      0x00000005 -> Shlib
      0x00000006 -> Phdr
      0x00000007 -> Tls
      else -> Other(value)
    }
  }
}
