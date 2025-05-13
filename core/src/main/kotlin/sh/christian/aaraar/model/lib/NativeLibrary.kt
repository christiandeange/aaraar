package sh.christian.aaraar.model.lib

import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

data class NativeLibrary
internal constructor(
  val fileHeader: NativeFileHeader,
  val programHeaders: List<NativeProgramHeader>,
  val sections: List<NativeSection>,
) {
  fun bytes(): ByteArray {
    val writer = NativeLibraryWriter()
    val context = writer.createWriteContext(this)

    with(writer) {
      context.writeFileHeader()
      context.writeProgramHeaders()
      context.writeSections()
    }

    return context.source.readByteArray()
  }

  fun writeTo(path: Path) {
    path.writeBytes(bytes())
  }

  companion object {
    fun from(path: Path): NativeLibrary {
      return from(path.readBytes())
    }

    fun from(bytes: ByteArray): NativeLibrary {
      val parser = NativeLibraryParser(bytes)
      val elfHeader = parser.parseElfHeader()
      val elfProgramHeaders = parser.parseProgramHeaders(elfHeader)
      val elfSections = parser.parseSections(elfHeader)

      val fileHeader = elfHeader.toNativeFileHeader()
      val parseContext = NativeLibraryParser.ParseContext(
        fileHeader = fileHeader,
        elfProgramHeaders = elfProgramHeaders,
        elfSections = elfSections,
      )

      val programHeaders = elfProgramHeaders.map { it.toNativeProgramHeader(elfSections) }
      val sections = elfSections.map { it.toNativeSection(parseContext) }

      return NativeLibrary(
        fileHeader = fileHeader,
        programHeaders = programHeaders,
        sections = sections,
      )
    }
  }
}
