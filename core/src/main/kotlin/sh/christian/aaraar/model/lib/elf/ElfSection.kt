package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.NativeSectionData
import sh.christian.aaraar.model.lib.NativeSectionFlag
import sh.christian.aaraar.model.lib.NativeSectionType
import sh.christian.aaraar.model.lib.Value

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
  fun toNativeSection(): NativeSection {
    return NativeSection(
      nameOffset = sh_name,
      type = NativeSectionType.from(sh_type),
      flags = NativeSectionFlag.from(sh_flags),
      virtualAddress = sh_addr,
      offset = sh_offset,
      size = sh_size,
      linkedSectionIndex = sh_link,
      extraInfo = sh_info,
      alignment = sh_addralign,
      entrySize = sh_entsize,
      data = NativeSectionData(data.data),
    )
  }
}
