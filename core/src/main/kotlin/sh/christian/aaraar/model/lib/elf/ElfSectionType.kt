package sh.christian.aaraar.model.lib.elf

sealed class ElfSectionType(val value: Int) {
  object Null : ElfSectionType(0x00000000)
  object Progbits : ElfSectionType(0x00000001)
  object Symtab : ElfSectionType(0x00000002)
  object Strtab : ElfSectionType(0x00000003)
  object Rela : ElfSectionType(0x00000004)
  object Hash : ElfSectionType(0x00000005)
  object Dynamic : ElfSectionType(0x00000006)
  object Note : ElfSectionType(0x00000007)
  object Nobits : ElfSectionType(0x00000008)
  object Rel : ElfSectionType(0x00000009)
  object Shlib : ElfSectionType(0x0000000A)
  object Dynsym : ElfSectionType(0x0000000B)
  object InitArray : ElfSectionType(0x0000000E)
  object FiniArray : ElfSectionType(0x0000000F)
  object PreinitArray : ElfSectionType(0x00000010)
  object Group : ElfSectionType(0x00000011)
  object SymtabShndx : ElfSectionType(0x00000012)
  object Num : ElfSectionType(0x00000013)

  class Other(value: Int) : ElfSectionType(value)

  override fun toString(): String {
    return "0x${value.toString(16).padStart(8, '0')}"
  }

  override fun equals(other: Any?): Boolean {
    return value == (other as? ElfSectionType)?.value
  }

  override fun hashCode(): Int {
    return value
  }

  companion object {
    fun from(value: Int): ElfSectionType = when (value) {
      0x00000000 -> Null
      0x00000001 -> Progbits
      0x00000002 -> Symtab
      0x00000003 -> Strtab
      0x00000004 -> Rela
      0x00000005 -> Hash
      0x00000006 -> Dynamic
      0x00000007 -> Note
      0x00000008 -> Nobits
      0x00000009 -> Rel
      0x0000000A -> Shlib
      0x0000000B -> Dynsym
      0x0000000E -> InitArray
      0x0000000F -> FiniArray
      0x00000010 -> PreinitArray
      0x00000011 -> Group
      0x00000012 -> SymtabShndx
      0x00000013 -> Num
      else -> Other(value)
    }
  }
}
