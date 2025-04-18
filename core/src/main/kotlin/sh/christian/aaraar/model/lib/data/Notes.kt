package sh.christian.aaraar.model.lib.data

import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import sh.christian.aaraar.model.lib.NativeLibrary
import sh.christian.aaraar.model.lib.NativeLibraryParser
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.Value.Value32
import sh.christian.aaraar.model.lib.elf.ElfSection
import sh.christian.aaraar.model.lib.elf.ElfSink
import sh.christian.aaraar.model.lib.elf.ElfSource

data class Notes(
  val type: Int,
  val name: String,
  val descriptor: ByteArray,
) : NativeSectionData {
  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray {
    val nameBytes = (name + "\u0000").encodeUtf8().toByteArray()

    val buffer = Buffer()
    val sink = ElfSink(lib.fileHeader.endianness, buffer)

    sink.int(nameBytes.size)
    sink.int(descriptor.size)
    sink.int(type)

    val namePadding = if (nameBytes.size % 4 == 0) 0L else 4L - (nameBytes.size % 4)
    val descriptorPadding = if (descriptor.size % 4 == 0) 0L else 4L - (descriptor.size % 4)

    sink.bytes(nameBytes)
    sink.skip(namePadding)
    sink.bytes(descriptor)
    sink.skip(descriptorPadding)

    return buffer.readByteArray()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Notes

    if (type != other.type) return false
    if (name != other.name) return false
    if (!descriptor.contentEquals(other.descriptor)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type
    result = 31 * result + name.hashCode()
    result = 31 * result + descriptor.contentHashCode()
    return result
  }

  companion object {
    fun from(
      parseContext: NativeLibraryParser.ParseContext,
      elfSection: ElfSection,
    ): Notes {
      val buffer = Buffer().apply { write(elfSection.data.data) }

      val source = ElfSource(
        architecture = parseContext.architecture,
        endianness = parseContext.endianness,
        source = buffer,
      )

      val namesz = source.int()
      val descsz = source.int()
      val type = source.int()

      val namePadding = if (namesz % 4 == 0) 0L else 4L - (namesz % 4)
      val descPadding = if (descsz % 4 == 0) 0L else 4L - (descsz % 4)

      val name = source.bytes(Value32(namesz))
      source.skip(namePadding)
      val desc = source.bytes(Value32(descsz))
      source.skip(descPadding)

      return Notes(
        type = type,
        name = name.toByteString().utf8().trimEnd('\u0000'),
        descriptor = desc,
      )
    }
  }
}
