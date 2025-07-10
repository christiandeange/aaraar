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

data class DynamicTable(
  val values: List<DynamicValue>,
) : NativeSectionData {
  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray {
    val buffer = Buffer()
    val sink = ElfSink(lib.fileHeader.endianness, buffer)

    values.forEach { symbol ->
      sink.value(
        when (lib.fileHeader.architecture) {
          BIT_32 -> Value32(symbol.tag.value)
          BIT_64 -> Value64(symbol.tag.value.toLong())
        }
      )
      sink.value(symbol.value)
    }

    return buffer.readByteArray()
  }

  companion object {
    fun from(
      parseContext: NativeLibraryParser.ParseContext,
      elfSection: ElfSection,
    ): DynamicTable {
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

      val dynamics = List(count) {
        DynamicValue(
          tag = DynamicValue.Tag(
            when (val tag = source.value()) {
              is Value32 -> tag.value
              is Value64 -> tag.value.toInt()
            }
          ),
          value = source.value(),
        )
      }

      return DynamicTable(values = dynamics)
    }
  }
}
