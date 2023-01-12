package sh.christian.aaraar.model

import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

class GenericJarArchive
private constructor(
  private val entries: Map<JarEntry, ByteArray>,
) {
  operator fun plus(other: GenericJarArchive): GenericJarArchive {
    return GenericJarArchive(entries + other.entries)
  }

  companion object {
    val NONE = GenericJarArchive(entries = emptyMap())

    fun from(path: Path): GenericJarArchive? {
      if (!Files.isRegularFile(path)) return null

      val jarInputStream = JarInputStream(Files.newInputStream(path))
      return jarInputStream.use { stream ->
        val indexedEntries = generateSequence { stream.nextJarEntry }
          .filterNot { it.isDirectory }
          .map { it to stream.readAllBytes() }
          .toMap()

        GenericJarArchive(indexedEntries)
      }
    }
  }
}
