package sh.christian.aaraar.model.lib

import sh.christian.aaraar.model.lib.elf.ElfFileHeader
import sh.christian.aaraar.model.lib.elf.ElfProgramHeader
import sh.christian.aaraar.model.lib.elf.ElfSection
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

data class NativeLibrary
internal constructor(
  val fileHeader: ElfFileHeader,
  val programHeaders: List<ElfProgramHeader>,
  val sections: List<ElfSection>,
) {
  fun bytes(): ByteArray {
    return NativeLibraryWriter(this).bytes()
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
      val programHeaders = parser.parseProgramHeaders(elfHeader)
      val sections = parser.parseSections(elfHeader)

      return NativeLibrary(
        fileHeader = elfHeader,
        programHeaders = programHeaders,
        sections = sections,
      )
    }
  }
}
