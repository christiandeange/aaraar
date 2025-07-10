package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.NativeLibraryParser
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.NativeSectionFlag
import sh.christian.aaraar.model.lib.NativeSectionType
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Dynamic
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Dynsym
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.FiniArray
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Group
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Hash
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.InitArray
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Nobits
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Note
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Null
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Num
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.PreinitArray
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Progbits
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Rel
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Rela
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Shlib
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Strtab
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.Symtab
import sh.christian.aaraar.model.lib.NativeSectionType.Companion.SymtabShndx
import sh.christian.aaraar.model.lib.Value
import sh.christian.aaraar.model.lib.data.Bytes
import sh.christian.aaraar.model.lib.data.DynamicTable
import sh.christian.aaraar.model.lib.data.Notes
import sh.christian.aaraar.model.lib.data.RelocationAddendTable
import sh.christian.aaraar.model.lib.data.RelocationTable
import sh.christian.aaraar.model.lib.data.StringTable
import sh.christian.aaraar.model.lib.data.SymbolTable

data class ElfSection(
  val sh_name: Int,
  val sh_type: Int,
  val sh_flags: Value,
  val sh_addr: Address,
  val sh_offset: Address,
  val sh_size: Value,
  val sh_link: Int,
  val sh_info: Int,
  val sh_addralign: Value,
  val sh_entsize: Value,
  val data: ElfSectionData,
) {
  fun toNativeSection(
    parseContext: NativeLibraryParser.ParseContext,
  ): NativeSection {
    val type = NativeSectionType(sh_type)

    return NativeSection(
      name = parseContext.sectionHeaderTable.stringAt(sh_name),
      type = type,
      flags = NativeSectionFlag.from(sh_flags),
      virtualAddress = sh_addr,
      linkedSectionIndex = sh_link,
      extraInfo = sh_info,
      alignment = sh_addralign,
      entrySize = sh_entsize,
      data = when (type) {
        Strtab -> StringTable.from(this)
        Symtab -> SymbolTable.from(parseContext, this)
        Dynsym -> SymbolTable.from(parseContext, this)
        Dynamic -> DynamicTable.from(parseContext, this)
        Note -> Notes.from(parseContext, this)
        Rel -> RelocationTable.from(parseContext, this)
        Rela -> RelocationAddendTable.from(parseContext, this)
        FiniArray,
        Group,
        Hash,
        InitArray,
        Nobits,
        Null,
        Num,
        PreinitArray,
        Progbits,
        Shlib,
        SymtabShndx -> {
          Bytes(data.data)
        }
        else -> {
          Bytes(data.data)
        }
      },
    )
  }
}
