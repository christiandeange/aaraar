package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.NativeEndian
import sh.christian.aaraar.model.lib.NativeFileHeader
import sh.christian.aaraar.model.lib.NativeFileHeaderType
import sh.christian.aaraar.model.lib.NativeFormat

data class ElfFileHeader(
  val ei_mag: String,
  val ei_class: Byte,
  val ei_data: Byte,
  val ei_version: Byte,
  val ei_osabi: Byte,
  val ei_abiversion: Byte,
  val e_type: Short,
  val e_machine: Short,
  val e_version: Int,
  val e_entry: Address,
  val e_phoff: Address,
  val e_shoff: Address,
  val e_flags: Int,
  val e_ehsize: Short,
  val e_phentsize: Short,
  val e_phnum: Short,
  val e_shentsize: Short,
  val e_shnum: Short,
  val e_shstrndx: Short,
) {
  fun toNativeFileHeader(): NativeFileHeader {
    return NativeFileHeader(
      architecture = NativeFormat.from(ei_class),
      endianness = NativeEndian.from(ei_data),
      operatingSystemAbi = ei_osabi,
      operatingSystemAbiVersion = ei_abiversion,
      fileType = NativeFileHeaderType(e_type),
      instructionSet = e_machine,
      flags = e_flags,
      fileHeaderSize = e_ehsize,
      programHeaderSize = e_phentsize,
      sectionHeaderSize = e_shentsize,
      sectionNamesHeaderIndex = e_shstrndx,
    )
  }
}
