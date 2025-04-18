package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.NativeLibraryParser
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.NativeSectionFlag
import sh.christian.aaraar.model.lib.NativeSectionType
import sh.christian.aaraar.model.lib.NativeSectionType.Dynamic
import sh.christian.aaraar.model.lib.NativeSectionType.Dynsym
import sh.christian.aaraar.model.lib.NativeSectionType.FiniArray
import sh.christian.aaraar.model.lib.NativeSectionType.Group
import sh.christian.aaraar.model.lib.NativeSectionType.Hash
import sh.christian.aaraar.model.lib.NativeSectionType.InitArray
import sh.christian.aaraar.model.lib.NativeSectionType.Nobits
import sh.christian.aaraar.model.lib.NativeSectionType.Note
import sh.christian.aaraar.model.lib.NativeSectionType.Null
import sh.christian.aaraar.model.lib.NativeSectionType.Num
import sh.christian.aaraar.model.lib.NativeSectionType.Other
import sh.christian.aaraar.model.lib.NativeSectionType.PreinitArray
import sh.christian.aaraar.model.lib.NativeSectionType.Progbits
import sh.christian.aaraar.model.lib.NativeSectionType.Rel
import sh.christian.aaraar.model.lib.NativeSectionType.Rela
import sh.christian.aaraar.model.lib.NativeSectionType.Shlib
import sh.christian.aaraar.model.lib.NativeSectionType.Strtab
import sh.christian.aaraar.model.lib.NativeSectionType.Symtab
import sh.christian.aaraar.model.lib.NativeSectionType.SymtabShndx
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
    val type = NativeSectionType.from(sh_type)

    return NativeSection(
      name = parseContext.sectionHeaderTable.stringAt(sh_name),
      type = type,
      flags = NativeSectionFlag.from(sh_flags),
      virtualAddress = sh_addr,
      offset = sh_offset,
      linkedSectionIndex = sh_link,
      extraInfo = sh_info,
      alignment = sh_addralign,
      entrySize = sh_entsize,
      data = when (type) {
        is Strtab -> StringTable.from(this)
        is Symtab -> SymbolTable.from(parseContext, this)
        is Dynsym -> SymbolTable.from(parseContext, this)
        is Dynamic -> DynamicTable.from(parseContext, this)
        is Note -> Notes.from(parseContext, this)
        is Rel -> RelocationTable.from(parseContext, this)
        is Rela -> RelocationAddendTable.from(parseContext, this)
        is FiniArray,
        is Group,
        is Hash,
        is InitArray,
        is Nobits,
        is Null,
        is Num,
        is Other,
        is PreinitArray,
        is Progbits,
        is Shlib,
        is SymtabShndx -> {
          Bytes(data.data)
        }
      },
    )
  }
}
