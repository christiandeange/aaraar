package sh.christian.aaraar.model.lib

import okio.Buffer
import okio.BufferedSource
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.NativeFormat.BIT_32
import sh.christian.aaraar.model.lib.NativeFormat.BIT_64
import sh.christian.aaraar.model.lib.NativeSectionFlag.Execinstr
import sh.christian.aaraar.model.lib.Value.Value32
import sh.christian.aaraar.model.lib.Value.Value64
import sh.christian.aaraar.model.lib.data.StringTable
import sh.christian.aaraar.model.lib.elf.ElfFileHeader
import sh.christian.aaraar.model.lib.elf.ElfProgramHeader
import sh.christian.aaraar.model.lib.elf.ElfSection
import sh.christian.aaraar.model.lib.elf.ElfSectionData
import sh.christian.aaraar.model.lib.elf.ElfSink

class NativeLibraryWriter {
  fun createWriteContext(lib: NativeLibrary): WriteContext {
    var addr = when (lib.fileHeader.architecture) {
      BIT_32 -> Address32(0)
      BIT_64 -> Address64(0)
    }

    addr += lib.fileHeader.fileHeaderSize

    val programHeaderStarts: List<Address> = buildList {
      repeat(lib.programHeaders.size) {
        add(addr)
        addr = (addr + lib.fileHeader.programHeaderSize)
      }
    }

    val sectionData = lib.sections.map { section ->
      section.data.bytes(lib, section)
    }

    val sectionStarts: List<Address> = buildList {
      lib.sections.forEachIndexed { i, section ->
        addr = addr.alignTo(section.alignment)
        add(addr)
        addr = (addr + sectionData[i].size)
      }
    }

    val sectionHeadersStarts: List<Address> = buildList {
      repeat(lib.sections.size) {
        add(addr)
        addr = (addr + lib.fileHeader.sectionHeaderSize)
      }
    }

    return WriteContext(
      lib = lib,
      programHeaderStarts = programHeaderStarts,
      sectionStarts = sectionStarts,
      sectionData = sectionData,
      sectionHeadersStarts = sectionHeadersStarts,
    )
  }

  fun WriteContext.writeFileHeader() {
    val elfFileHeader = computeElfFileHeader(this)

    with(elfFileHeader) {
      sink.bytes(ei_mag.toByteArray())
      sink.byte(ei_class)
      sink.byte(ei_data)
      sink.byte(ei_version)
      sink.byte(ei_osabi)
      sink.byte(ei_abiversion)
      sink.skip(7)
      sink.short(e_type)
      sink.short(e_machine)
      sink.int(e_version)
      sink.address(e_entry)
      sink.address(e_phoff)
      sink.address(e_shoff)
      sink.int(e_flags)
      sink.short(e_ehsize)
      sink.short(e_phentsize)
      sink.short(e_phnum)
      sink.short(e_shentsize)
      sink.short(e_shnum)
      sink.short(e_shstrndx)
    }
  }

  fun WriteContext.writeProgramHeaders() {
    lib.programHeaders.forEachIndexed { i, programHeader ->
      val elfProgramHeader = computeElfProgramHeader(programHeader)
      writeProgramHeader(i, elfProgramHeader)
    }
  }

  fun WriteContext.writeProgramHeader(
    index: Int,
    elfProgramHeader: ElfProgramHeader,
  ) {
    sink.skipTo(programHeaderStarts[index])

    with(elfProgramHeader) {
      sink.int(p_type)
      if (lib.fileHeader.architecture == BIT_64) {
        sink.int(p_flags)
      }
      sink.address(p_offset)
      sink.address(p_vaddr)
      sink.address(p_paddr)
      sink.value(p_filesz)
      sink.value(p_memsz)
      if (lib.fileHeader.architecture == BIT_32) {
        sink.int(p_flags)
      }
      sink.value(p_align)
    }
  }

  fun WriteContext.writeSections() {
    val elfSections = lib.sections.zip(sectionData) { section, data ->
      computeElfSection(lib, section, data)
    }

    elfSections.forEachIndexed { i, elfSection ->
      writeSection(i, elfSection)
    }
    elfSections.forEachIndexed { i, elfSection ->
      writeSectionHeader(i, elfSection)
    }
  }

  fun WriteContext.writeSection(
    index: Int,
    section: ElfSection,
  ) {
    sink.skipTo(sectionStarts[index])
    sink.bytes(section.data.data)
  }

  fun WriteContext.writeSectionHeader(
    index: Int,
    sectionHeader: ElfSection,
  ) {
    sink.skipTo(sectionHeadersStarts[index])

    with(sectionHeader) {
      sink.int(sh_name)
      sink.int(sh_type)
      sink.value(sh_flags)
      sink.address(sh_addr)
      sink.address(sh_offset)
      sink.value(sh_size)
      sink.int(sh_link)
      sink.int(sh_info)
      sink.value(sh_addralign)
      sink.value(sh_entsize)
    }
  }

  private fun computeElfFileHeader(context: WriteContext): ElfFileHeader {
    val nativeFileHeader = context.lib.fileHeader

    return ElfFileHeader(
      ei_mag = "\u007fELF",
      ei_class = nativeFileHeader.architecture.value,
      ei_data = nativeFileHeader.endianness.value,
      ei_version = 1,
      ei_osabi = nativeFileHeader.operatingSystemAbi,
      ei_abiversion = nativeFileHeader.operatingSystemAbiVersion,
      e_type = nativeFileHeader.fileType.value,
      e_machine = nativeFileHeader.instructionSet,
      e_version = 1,
      e_entry = context.sectionStarts[context.lib.sections.indexOfFirst { it.flags.contains(Execinstr) }],
      e_phoff = context.programHeaderStarts.first(),
      e_shoff = context.sectionHeadersStarts.first(),
      e_flags = nativeFileHeader.flags,
      e_ehsize = nativeFileHeader.fileHeaderSize,
      e_phentsize = nativeFileHeader.programHeaderSize,
      e_phnum = context.programHeaderStarts.size.toShort(),
      e_shentsize = nativeFileHeader.sectionHeaderSize,
      e_shnum = context.sectionHeadersStarts.size.toShort(),
      e_shstrndx = nativeFileHeader.sectionNamesHeaderIndex,
    )
  }

  private fun WriteContext.computeElfProgramHeader(nativeProgramHeader: NativeProgramHeader): ElfProgramHeader {
    return ElfProgramHeader(
      p_type = nativeProgramHeader.type.value,
      p_flags = nativeProgramHeader.flags.fold(0) { acc, flag -> acc or flag.value },
      p_offset = when (val offsetSource = nativeProgramHeader.offset) {
        is AddressReference.Zero -> {
          when (lib.fileHeader.architecture) {
            BIT_32 -> Address32(0)
            BIT_64 -> Address64(0)
          }
        }
        is AddressReference.ProgramHeaderStart -> {
          programHeaderStarts[offsetSource.index]
        }
        is AddressReference.SectionStart -> {
          sectionStarts[offsetSource.index]
        }
      },
      p_vaddr = nativeProgramHeader.virtualAddress,
      p_paddr = nativeProgramHeader.virtualAddress,
      p_filesz = nativeProgramHeader.segmentFileSize,
      p_memsz = nativeProgramHeader.segmentVirtualSize,
      p_align = nativeProgramHeader.alignment,
    )
  }

  private fun WriteContext.computeElfSection(
    lib: NativeLibrary,
    nativeSection: NativeSection,
    data: ByteArray,
  ): ElfSection {
    val sectionNames = lib.sections[lib.fileHeader.sectionNamesHeaderIndex.toInt()].data as StringTable

    return ElfSection(
      sh_name = sectionNames.offsetOf(nativeSection.name),
      sh_type = nativeSection.type.value,
      sh_flags = when (lib.fileHeader.architecture) {
        BIT_32 -> Value32(nativeSection.flags.fold(0) { acc, flag -> acc or flag.value })
        BIT_64 -> Value64(nativeSection.flags.fold(0L) { acc, flag -> acc or flag.value.toLong() })
      },
      sh_addr = nativeSection.virtualAddress,
      sh_offset = if (nativeSection.type == NativeSectionType.Null) {
        when (lib.fileHeader.architecture) {
          BIT_32 -> Address32(0)
          BIT_64 -> Address64(0)
        }
      } else {
        sectionStarts[lib.sections.indexOf(nativeSection)]
      },
      sh_size = when (lib.fileHeader.architecture) {
        BIT_32 -> Value32(data.size)
        BIT_64 -> Value64(data.size.toLong())
      },
      sh_link = nativeSection.linkedSectionIndex,
      sh_info = nativeSection.extraInfo,
      sh_addralign = nativeSection.alignment,
      sh_entsize = nativeSection.entrySize,
      data = ElfSectionData(data),
    )
  }

  class WriteContext(
    val lib: NativeLibrary,
    val programHeaderStarts: List<Address>,
    val sectionStarts: List<Address>,
    val sectionData: List<ByteArray>,
    val sectionHeadersStarts: List<Address>,
  ) {
    private val buffer = Buffer()

    val sink = ElfSink(lib.fileHeader.endianness, buffer)
    val source: BufferedSource = buffer
  }
}
