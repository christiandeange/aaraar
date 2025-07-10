package sh.christian.aaraar.model.lib

data class NativeSectionType(val value: Int) {
  override fun toString(): String = when (this) {
    Null -> "Null"
    Progbits -> "Progbits"
    Symtab -> "Symtab"
    Strtab -> "Strtab"
    Rela -> "Rela"
    Hash -> "Hash"
    Dynamic -> "Dynamic"
    Note -> "Note"
    Nobits -> "Nobits"
    Rel -> "Rel"
    Shlib -> "Shlib"
    Dynsym -> "Dynsym"
    InitArray -> "InitArray"
    FiniArray -> "FiniArray"
    PreinitArray -> "PreinitArray"
    Group -> "Group"
    SymtabShndx -> "SymtabShndx"
    Num -> "Num"
    else -> "Other(0x${value.toString(16)})"
  }

  companion object {
    val Null = NativeSectionType(0x00000000)
    val Progbits = NativeSectionType(0x00000001)
    val Symtab = NativeSectionType(0x00000002)
    val Strtab = NativeSectionType(0x00000003)
    val Rela = NativeSectionType(0x00000004)
    val Hash = NativeSectionType(0x00000005)
    val Dynamic = NativeSectionType(0x00000006)
    val Note = NativeSectionType(0x00000007)
    val Nobits = NativeSectionType(0x00000008)
    val Rel = NativeSectionType(0x00000009)
    val Shlib = NativeSectionType(0x0000000A)
    val Dynsym = NativeSectionType(0x0000000B)
    val InitArray = NativeSectionType(0x0000000E)
    val FiniArray = NativeSectionType(0x0000000F)
    val PreinitArray = NativeSectionType(0x00000010)
    val Group = NativeSectionType(0x00000011)
    val SymtabShndx = NativeSectionType(0x00000012)
    val Num = NativeSectionType(0x00000013)
  }
}
