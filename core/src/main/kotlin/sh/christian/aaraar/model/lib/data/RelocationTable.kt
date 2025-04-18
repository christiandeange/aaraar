package sh.christian.aaraar.model.lib.data

import okio.Buffer
import sh.christian.aaraar.model.lib.NativeFormat.BIT_32
import sh.christian.aaraar.model.lib.NativeFormat.BIT_64
import sh.christian.aaraar.model.lib.NativeLibrary
import sh.christian.aaraar.model.lib.NativeLibraryParser
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.Value.Value32
import sh.christian.aaraar.model.lib.Value.Value64
import sh.christian.aaraar.model.lib.elf.ElfSection
import sh.christian.aaraar.model.lib.elf.ElfSink
import sh.christian.aaraar.model.lib.elf.ElfSource

data class RelocationTable(
  val values: List<RelocationValue>,
) : NativeSectionData {
  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray {
    val symbolTable = lib.sections[section.linkedSectionIndex].data as SymbolTable

    val buffer = Buffer()
    val sink = ElfSink(lib.fileHeader.endianness, buffer)

    values.forEach { relocation ->
      val symbolIndex = symbolTable.symbols.indexOfFirst { it.name == relocation.symbolName }
      require(symbolIndex >= 0) {
        "Symbol '${relocation.symbolName}' not found in symbol table"
      }

      sink.address(relocation.offset)
      sink.value(
        when (lib.fileHeader.architecture) {
          BIT_32 -> Value32((symbolIndex shl 8) or (relocation.type and 0xFF))
          BIT_64 -> Value64((symbolIndex.toLong() shl 32) or (relocation.type.toLong() and 0xFFFFFFFF))
        }
      )
    }

    return buffer.readByteArray()
  }

  companion object {
    fun from(
      parseContext: NativeLibraryParser.ParseContext,
      elfSection: ElfSection,
    ): RelocationTable {
      val symbolTable = SymbolTable.from(parseContext, parseContext.elfSections[elfSection.sh_link])

      val buffer = Buffer().apply { write(elfSection.data.data) }

      val source = ElfSource(
        architecture = parseContext.architecture,
        endianness = parseContext.endianness,
        source = buffer,
      )

      val entrySize = when (val entSize = elfSection.sh_entsize) {
        is Value32 -> entSize.value
        is Value64 -> entSize.value.toInt()
      }
      val count = elfSection.data.data.size / entrySize

      val relocations = List(count) {
        val offset = source.address()
        val info = source.value()

        val symbolIndex = when (info) {
          is Value32 -> (info.value ushr 8)
          is Value64 -> (info.value ushr 32).toInt()
        }

        val type = when (info) {
          is Value32 -> info.value and 0xFF
          is Value64 -> info.value and 0xFFFFFFFF
        }.toInt()

        RelocationValue(
          offset = offset,
          symbolName = symbolTable.symbols[symbolIndex].name,
          type = type,
        )
      }

      return RelocationTable(values = relocations)
    }
  }
}
