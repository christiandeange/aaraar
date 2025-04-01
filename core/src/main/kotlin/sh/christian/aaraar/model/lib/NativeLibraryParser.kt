package sh.christian.aaraar.model.lib

import okio.Buffer
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.NativeFormat.BIT_32
import sh.christian.aaraar.model.lib.NativeFormat.BIT_64
import sh.christian.aaraar.model.lib.elf.ElfFileHeader
import sh.christian.aaraar.model.lib.elf.ElfProgramHeader
import sh.christian.aaraar.model.lib.elf.ElfSection
import sh.christian.aaraar.model.lib.elf.ElfSectionData
import sh.christian.aaraar.model.lib.elf.ElfSource

class NativeLibraryParser(
  private val sourceBytes: ByteArray,
) {
  private fun newElfSource(
    offset: Address,
    elfHeader: ElfFileHeader,
  ): ElfSource {
    return newElfSource(
      offset = offset,
      identifierClass = NativeFormat.from(elfHeader.ei_class),
      identifierData = NativeEndian.from(elfHeader.ei_data),
    )
  }

  private fun newElfSource(
    offset: Address,
    identifierClass: NativeFormat,
    identifierData: NativeEndian,
  ): ElfSource {
    val startPos = when (offset) {
      is Address32 -> offset.value
      is Address64 -> offset.value.toInt()
    }
    val buffer = Buffer().apply {
      write(sourceBytes, startPos, sourceBytes.size - startPos)
    }

    return ElfSource(
      identifierClass = identifierClass,
      identifierData = identifierData,
      source = buffer,
    )
  }

  fun parseElfHeader(): ElfFileHeader {
    val identifierBytes = Buffer().apply { write(sourceBytes, 0x00, 0x06) }

    val ei_mag = identifierBytes.readUtf8(4)
    check(ei_mag == "\u007fELF") { "Not an ELF file" }

    val ei_class = identifierBytes.readByte()
    val ei_data = identifierBytes.readByte()

    val bytes = newElfSource(
      offset = Address32(0x06),
      identifierClass = NativeFormat.from(ei_class),
      identifierData = NativeEndian.from(ei_data),
    )
    val ei_version = bytes.byte()
    val ei_osabi = bytes.byte()
    val ei_abiversion = bytes.byte()
    bytes.skip(7)
    val e_type = bytes.short()
    val e_machine = bytes.short()
    val e_version = bytes.int()
    val e_entry = bytes.address()
    val e_phoff = bytes.address()
    val e_shoff = bytes.address()
    val e_flags = bytes.int()
    val e_ehsize = bytes.short()
    val e_phentsize = bytes.short()
    val e_phnum = bytes.short()
    val e_shentsize = bytes.short()
    val e_shnum = bytes.short()
    val e_shstrndx = bytes.short()

    return ElfFileHeader(
      ei_mag = ei_mag,
      ei_class = ei_class,
      ei_data = ei_data,
      ei_version = ei_version,
      ei_osabi = ei_osabi,
      ei_abiversion = ei_abiversion,
      e_type = e_type,
      e_machine = e_machine,
      e_version = e_version,
      e_entry = e_entry,
      e_phoff = e_phoff,
      e_shoff = e_shoff,
      e_flags = e_flags,
      e_ehsize = e_ehsize,
      e_phentsize = e_phentsize,
      e_phnum = e_phnum,
      e_shentsize = e_shentsize,
      e_shnum = e_shnum,
      e_shstrndx = e_shstrndx,
    )
  }

  fun parseProgramHeaders(elfHeader: ElfFileHeader): List<ElfProgramHeader> {
    return List(elfHeader.e_phnum.toInt()) { i ->
      val address = elfHeader.e_phoff + i * elfHeader.e_phentsize
      parseProgramHeader(address, elfHeader)
    }
  }

  fun parseProgramHeader(
    address: Address,
    elfHeader: ElfFileHeader,
  ): ElfProgramHeader {
    val bytes = newElfSource(address, elfHeader)

    val p_type = bytes.int()
    val flags64 = if (elfHeader.ei_class == BIT_64.value) bytes.int() else null
    val p_offset = bytes.address()
    val p_vaddr = bytes.address()
    val p_paddr = bytes.address()
    val p_filesz = bytes.value()
    val p_memsz = bytes.value()
    val flags32 = if (elfHeader.ei_class == BIT_32.value) bytes.int() else null
    val p_align = bytes.value()
    val p_flags = requireNotNull(flags64 ?: flags32)

    return ElfProgramHeader(
      p_type = p_type,
      p_flags = p_flags,
      p_offset = p_offset,
      p_vaddr = p_vaddr,
      p_paddr = p_paddr,
      p_filesz = p_filesz,
      p_memsz = p_memsz,
      p_align = p_align,
    )
  }

  fun parseSections(elfHeader: ElfFileHeader): List<ElfSection> {
    return List(elfHeader.e_shnum.toInt()) { i ->
      val address = elfHeader.e_shoff + i * elfHeader.e_shentsize
      parseSection(address, elfHeader)
    }
  }

  fun parseSection(
    address: Address,
    elfHeader: ElfFileHeader
  ): ElfSection {
    val bytes = newElfSource(address, elfHeader)

    val sh_name = bytes.int()
    val sh_type = bytes.int()
    val sh_flags = bytes.value()
    val sh_addr = bytes.address()
    val sh_offset = bytes.address()
    val sh_size = bytes.value()
    val sh_link = bytes.int()
    val sh_info = bytes.int()
    val sh_addralign = bytes.value()
    val sh_entsize = bytes.value()

    val dataBytes = newElfSource(sh_offset, elfHeader)
    val dataByteArray = dataBytes.bytes(sh_size)
    val data = ElfSectionData(data = dataByteArray)

    return ElfSection(
      sh_name = sh_name,
      sh_type = sh_type,
      sh_flags = sh_flags,
      sh_addr = sh_addr,
      sh_offset = sh_offset,
      sh_size = sh_size,
      sh_link = sh_link,
      sh_info = sh_info,
      sh_addralign = sh_addralign,
      sh_entsize = sh_entsize,
      data = data,
    )
  }
}
