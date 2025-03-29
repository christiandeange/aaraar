package sh.christian.aaraar.model.lib

import okio.Buffer
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.elf.ElfEndian
import sh.christian.aaraar.model.lib.elf.ElfEndian.BIG
import sh.christian.aaraar.model.lib.elf.ElfEndian.LITTLE
import sh.christian.aaraar.model.lib.elf.ElfFileHeader
import sh.christian.aaraar.model.lib.elf.ElfFileHeaderType
import sh.christian.aaraar.model.lib.elf.ElfFormat
import sh.christian.aaraar.model.lib.elf.ElfFormat.BIT_32
import sh.christian.aaraar.model.lib.elf.ElfFormat.BIT_64
import sh.christian.aaraar.model.lib.elf.ElfProgramHeader
import sh.christian.aaraar.model.lib.elf.ElfProgramHeaderFlag
import sh.christian.aaraar.model.lib.elf.ElfProgramHeaderType
import sh.christian.aaraar.model.lib.elf.ElfSection
import sh.christian.aaraar.model.lib.elf.ElfSectionData
import sh.christian.aaraar.model.lib.elf.ElfSectionFlag
import sh.christian.aaraar.model.lib.elf.ElfSectionType
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
      identifierClass = elfHeader.identifierClass,
      identifierData = elfHeader.identifierData,
    )
  }

  private fun newElfSource(
    offset: Address,
    identifierClass: ElfFormat,
    identifierData: ElfEndian,
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

    val identifierMagic = identifierBytes.readUtf8(4)
    check(identifierMagic == "\u007fELF") { "Not an ELF file" }

    val identifierClass: ElfFormat = when (val cls = identifierBytes.readByte()) {
      1.toByte() -> BIT_32
      2.toByte() -> BIT_64
      else -> throw IllegalArgumentException("Invalid address class: $cls")
    }
    val identifierData: ElfEndian = when (val data = identifierBytes.readByte()) {
      1.toByte() -> LITTLE
      2.toByte() -> BIG
      else -> throw IllegalArgumentException("Invalid endianness: $data")
    }

    val bytes = newElfSource(
      offset = Address32(0x06),
      identifierClass = identifierClass,
      identifierData = identifierData,
    )
    val identifierVersion = bytes.byte()
    val identifierOsAbi = bytes.byte()
    val identifierAbiVersion = bytes.byte()
    bytes.skip(7)
    val identifierType = ElfFileHeaderType.from(bytes.short())
    val machine = bytes.short()
    val version = bytes.int()
    val entry = bytes.address()
    val phOff = bytes.address()
    val shOff = bytes.address()
    val flags = bytes.int()
    val ehSize = bytes.short()
    val phEntSize = bytes.short()
    val phNum = bytes.short()
    val shEntSize = bytes.short()
    val shNum = bytes.short()
    val shStrNdx = bytes.short()

    return ElfFileHeader(
      identifierMagic = identifierMagic,
      identifierClass = identifierClass,
      identifierData = identifierData,
      identifierVersion = identifierVersion,
      identifierOsAbi = identifierOsAbi,
      identifierAbiVersion = identifierAbiVersion,
      identifierType = identifierType,
      machine = machine,
      version = version,
      entry = entry,
      phOff = phOff,
      shOff = shOff,
      flags = flags,
      ehSize = ehSize,
      phEntSize = phEntSize,
      phNum = phNum,
      shEntSize = shEntSize,
      shNum = shNum,
      shStrNdx = shStrNdx,
    )
  }

  fun parseProgramHeaders(elfHeader: ElfFileHeader): List<ElfProgramHeader> {
    return List(elfHeader.phNum.toInt()) { i ->
      val address = elfHeader.phOff + i * elfHeader.phEntSize
      parseProgramHeader(address, elfHeader)
    }
  }

  fun parseProgramHeader(
    address: Address,
    elfHeader: ElfFileHeader,
  ): ElfProgramHeader {
    val bytes = newElfSource(address, elfHeader)

    val type = ElfProgramHeaderType.from(bytes.int())
    val flags32 = when (elfHeader.identifierClass) {
      BIT_32 -> null
      BIT_64 -> bytes.int()
    }
    val offset = bytes.address()
    val vAddr = bytes.address()
    val pAddr = bytes.address()
    val fileSize = bytes.value()
    val memSize = bytes.value()
    val flags64 = when (elfHeader.identifierClass) {
      BIT_32 -> bytes.int()
      BIT_64 -> null
    }
    val align = bytes.value()
    val flags = ElfProgramHeaderFlag.from(requireNotNull(flags64 ?: flags32))

    return ElfProgramHeader(
      type = type,
      flags = flags,
      offset = offset,
      vAddr = vAddr,
      pAddr = pAddr,
      fileSize = fileSize,
      memSize = memSize,
      align = align,
    )
  }

  fun parseSections(elfHeader: ElfFileHeader): List<ElfSection> {
    return List(elfHeader.shNum.toInt()) { i ->
      val address = elfHeader.shOff + i * elfHeader.shEntSize
      parseSection(address, elfHeader)
    }
  }

  fun parseSection(
    address: Address,
    elfHeader: ElfFileHeader
  ): ElfSection {
    val bytes = newElfSource(address, elfHeader)

    val name = bytes.int()
    val type = ElfSectionType.from(bytes.int())
    val flags = ElfSectionFlag.from(bytes.value())
    val addr = bytes.address()
    val offset = bytes.address()
    val size = bytes.value()
    val link = bytes.int()
    val info = bytes.int()
    val addrAlign = bytes.value()
    val entrySize = bytes.value()

    val dataBytes = newElfSource(offset, elfHeader)
    val dataByteArray = dataBytes.bytes(size)
    val data = ElfSectionData(data = dataByteArray)

    return ElfSection(
      name = name,
      type = type,
      flags = flags,
      addr = addr,
      offset = offset,
      size = size,
      link = link,
      info = info,
      addrAlign = addrAlign,
      entrySize = entrySize,
      data = data,
    )
  }
}
