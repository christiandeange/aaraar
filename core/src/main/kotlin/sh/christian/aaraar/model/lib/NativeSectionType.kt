package sh.christian.aaraar.model.lib

sealed class NativeSectionType(val value: Int) {
  object Null : NativeSectionType(0x00000000)
  object Progbits : NativeSectionType(0x00000001)
  object Symtab : NativeSectionType(0x00000002)
  object Strtab : NativeSectionType(0x00000003)
  object Rela : NativeSectionType(0x00000004)
  object Hash : NativeSectionType(0x00000005)
  object Dynamic : NativeSectionType(0x00000006)
  object Note : NativeSectionType(0x00000007)
  object Nobits : NativeSectionType(0x00000008)
  object Rel : NativeSectionType(0x00000009)
  object Shlib : NativeSectionType(0x0000000A)
  object Dynsym : NativeSectionType(0x0000000B)
  object InitArray : NativeSectionType(0x0000000E)
  object FiniArray : NativeSectionType(0x0000000F)
  object PreinitArray : NativeSectionType(0x00000010)
  object Group : NativeSectionType(0x00000011)
  object SymtabShndx : NativeSectionType(0x00000012)
  object Num : NativeSectionType(0x00000013)

  class Other(value: Int) : NativeSectionType(value)

  override fun toString(): String {
    return "0x${value.toString(16).padStart(Int.SIZE_BYTES * 2, '0')}"
  }

  override fun equals(other: Any?): Boolean {
    return value == (other as? NativeSectionType)?.value
  }

  override fun hashCode(): Int {
    return value
  }

  companion object {
    fun from(value: Int): NativeSectionType = when (value) {
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
