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

data class SymbolTable(
  val symbols: List<Symbol>,
) : NativeSectionData {
  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray {
    val buffer = Buffer()
    val sink = ElfSink(lib.fileHeader.endianness, buffer)

    val stringTable = lib.sections[section.linkedSectionIndex].data as StringTable

    symbols.forEach { symbol ->
      when (lib.fileHeader.architecture) {
        BIT_32 -> {
          sink.int(stringTable.offsetOf(symbol.name))
          sink.value(symbol.value)
          sink.value(symbol.size)
          sink.byte(symbol.info)
          sink.byte(symbol.other)
          sink.short(symbol.sectionIndex)
        }
        BIT_64 -> {
          sink.int(stringTable.offsetOf(symbol.name))
          sink.byte(symbol.info)
          sink.byte(symbol.other)
          sink.short(symbol.sectionIndex)
          sink.value(symbol.value)
          sink.value(symbol.size)
        }
      }
    }

    return buffer.readByteArray()
  }

  companion object {
    fun from(
      parseContext: NativeLibraryParser.ParseContext,
      elfSection: ElfSection,
    ): SymbolTable {
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

      val stringTable = StringTable.from(parseContext.elfSections[elfSection.sh_link])

      val symbols = List(count) {
        when (parseContext.architecture) {
          BIT_32 -> {
            val st_name = source.int()
            val st_value = source.value()
            val st_size = source.value()
            val st_info = source.byte()
            val st_other = source.byte()
            val st_shndx = source.short()

            Symbol(
              name = stringTable.stringAt(st_name),
              binding = Symbol.Binding.fromInfo(st_info),
              type = Symbol.Type.fromInfo(st_info),
              other = st_other,
              sectionIndex = st_shndx,
              value = st_value,
              size = st_size,
            )
          }
          BIT_64 -> {
            val st_name = source.int()
            val st_info = source.byte()
            val st_other = source.byte()
            val st_shndx = source.short()
            val st_value = source.value()
            val st_size = source.value()

            Symbol(
              name = stringTable.stringAt(st_name),
              binding = Symbol.Binding.fromInfo(st_info),
              type = Symbol.Type.fromInfo(st_info),
              other = st_other,
              sectionIndex = st_shndx,
              value = st_value,
              size = st_size,
            )
          }
        }
      }

      return SymbolTable(symbols = symbols)
    }
  }
}
