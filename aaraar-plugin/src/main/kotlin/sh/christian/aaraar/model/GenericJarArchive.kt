package sh.christian.aaraar.model

import sh.christian.aaraar.utils.createJar
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.mkdirs
import java.nio.file.CopyOption
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

  fun writeTo(path: Path) {
    val tempClassesJar = Files.createTempFile("classes", ".jar").deleteIfExists()

    tempClassesJar.createJar { classesJar ->
      entries.forEach { (entry, contents) ->
        Files.write((classesJar / entry.name).mkdirs(), contents)
      }
    }

    Files.copy(tempClassesJar, path)
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
