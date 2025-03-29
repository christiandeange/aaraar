package sh.christian.aaraar.model.lib

import okio.Buffer
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.elf.ElfEndian.BIG
import sh.christian.aaraar.model.lib.elf.ElfEndian.LITTLE
import sh.christian.aaraar.model.lib.elf.ElfFileHeader
import sh.christian.aaraar.model.lib.elf.ElfFormat.BIT_32
import sh.christian.aaraar.model.lib.elf.ElfFormat.BIT_64
import sh.christian.aaraar.model.lib.elf.ElfProgramHeader
import sh.christian.aaraar.model.lib.elf.ElfSection
import sh.christian.aaraar.model.lib.elf.ElfSink

class NativeLibraryWriter(
  private val lib: NativeLibrary,
) {
  fun bytes(): ByteArray {
    var addr = when (lib.fileHeader.identifierClass) {
      BIT_32 -> Address32(0)
      BIT_64 -> Address64(0)
    }

    addr += lib.fileHeader.ehSize

    val programHeaderStarts: List<Address> = buildList {
      repeat(lib.programHeaders.size) {
        add(addr)
        addr = (addr + lib.fileHeader.phEntSize)
      }
    }

    val sectionStarts: List<Address> = buildList {
      lib.sections.forEach {
        addr = addr.alignTo(it.addrAlign)
        add(addr)
        addr = (addr + it.data.data.size)
      }
    }

    val sectionHeadersStarts: List<Address> = buildList {
      repeat(lib.sections.size) {
        add(addr)
        addr = (addr + lib.fileHeader.shEntSize)
      }
    }

    val fileHeaderToWrite = lib.fileHeader.copy(
      phOff = programHeaderStarts.first(),
      shOff = sectionHeadersStarts.first(),
    )
    val programHeadersToWrite = lib.programHeaders
    val sectionsToWrite = lib.sections

    val buffer = Buffer()
    val sink = ElfSink(fileHeaderToWrite.identifierData, buffer)

    fileHeaderToWrite.writeTo(sink)
    programHeadersToWrite.forEachIndexed { i, programHeader ->
      sink.skipTo(programHeaderStarts[i])
      programHeader.writeTo(sink)
    }
    sectionsToWrite.forEachIndexed { i, section ->
      sink.skipTo(sectionStarts[i])
      section.writeSectionTo(sink)
    }
    sectionsToWrite.forEachIndexed { i, section ->
      sink.skipTo(sectionHeadersStarts[i])
      section.writeSectionHeaderTo(sink)
    }

    return buffer.readByteArray()
  }

  private fun ElfFileHeader.writeTo(sink: ElfSink) {
    sink.bytes(identifierMagic.toByteArray())
    sink.byte(
      when (identifierClass) {
        BIT_32 -> 1
        BIT_64 -> 2
      }
    )
    sink.byte(
      when (identifierData) {
        LITTLE -> 1
        BIG -> 2
      }
    )
    sink.byte(identifierVersion)
    sink.byte(identifierOsAbi)
    sink.byte(identifierAbiVersion)
    sink.skip(7)
    sink.short(identifierType.value)
    sink.short(machine)
    sink.int(version)
    sink.address(entry)
    sink.address(phOff)
    sink.address(shOff)
    sink.int(flags)
    sink.short(ehSize)
    sink.short(phEntSize)
    sink.short(phNum)
    sink.short(shEntSize)
    sink.short(shNum)
    sink.short(shStrNdx)
  }

  private fun ElfProgramHeader.writeTo(sink: ElfSink) {
    sink.int(type.value)
    if (lib.fileHeader.identifierClass == BIT_64) {
      sink.int(flags.fold(0) { acc, flag -> acc or flag.value })
    }
    sink.address(offset)
    sink.address(vAddr)
    sink.address(pAddr)
    sink.value(fileSize)
    sink.value(memSize)
    if (lib.fileHeader.identifierClass == BIT_32) {
      sink.int(flags.fold(0) { acc, flag -> acc or flag.value })
    }
    sink.value(align)
  }

  private fun ElfSection.writeSectionTo(sink: ElfSink) {
    sink.bytes(data.data)
  }

  private fun ElfSection.writeSectionHeaderTo(sink: ElfSink) {
    sink.int(name)
    sink.int(type.value)
    when (lib.fileHeader.identifierClass) {
      BIT_32 -> sink.int(flags.fold(0) { acc, flag -> acc or flag.value })
      BIT_64 -> sink.long(flags.fold(0) { acc, flag -> acc or flag.value.toLong() })
    }
    sink.address(addr)
    sink.address(offset)
    sink.value(size)
    sink.int(link)
    sink.int(info)
    sink.value(addrAlign)
    sink.value(entrySize)
  }
}
