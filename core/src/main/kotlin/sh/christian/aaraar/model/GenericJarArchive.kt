package sh.christian.aaraar.model

import sh.christian.aaraar.utils.createArchive
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.mkdirs
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.jar.JarInputStream
import kotlin.streams.asSequence

/**
 * Represents an arbitrary set of `jar` entries, indexed by their relative file path to the root folder.
 */
class GenericJarArchive(
  val archiveEntries: Map<String, ByteArray>,
) : Map<String, ByteArray> by archiveEntries {
  fun bytes(): ByteArray {
    if (isEmpty()) return byteArrayOf()

    val tempJarFile = Files.createTempFile("out", ".jar").deleteIfExists()
    return try {
      writeTo(tempJarFile)
      Files.readAllBytes(tempJarFile)
    } finally {
      tempJarFile.deleteIfExists()
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other !is GenericJarArchive) return false
    return contentEquals(archiveEntries, other.archiveEntries)
  }

  override fun hashCode(): Int {
    return contentHashCode(archiveEntries)
  }

  fun writeTo(path: Path) {
    if (isEmpty()) {
      Files.deleteIfExists(path)
      return
    }

    val tempClassesJar = Files.createTempFile("classes", ".jar").deleteIfExists()

    tempClassesJar.createArchive { classesJar ->
      entries.forEach { (entry, contents) ->
        val entryPath = (classesJar / entry).mkdirs()
        Files.write(entryPath, contents)
      }

      classesJar.rootDirectories
        .asSequence()
        .flatMap { Files.walk(it).asSequence() - setOf(it) }
        .forEach { Files.setLastModifiedTime(it, EPOCH) }
    }

    Files.move(tempClassesJar, path)
  }

  companion object {
    val NONE = GenericJarArchive(archiveEntries = emptyMap())

    private val EPOCH = FileTime.fromMillis(0L)

    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): GenericJarArchive? {
      return if (!Files.isRegularFile(path)) {
        null
      } else {
        from(Files.newInputStream(path), keepMetaFiles)
      }
    }

    fun from(
      bytes: ByteArray,
      keepMetaFiles: Boolean,
    ): GenericJarArchive? {
      return if (bytes.isEmpty()) {
        null
      } else {
        from(bytes.inputStream(), keepMetaFiles)
      }
    }

    fun from(
      byteStream: InputStream,
      keepMetaFiles: Boolean,
    ): GenericJarArchive {
      return JarInputStream(byteStream).use { stream ->
        val indexedEntries = generateSequence { stream.nextJarEntry }
          .filterNot { it.isDirectory }
          .filter { !it.name.startsWith("META-INF/") || keepMetaFiles }
          .map { it.name to stream.readAllBytes() }
          .toMap()

        GenericJarArchive(indexedEntries)
      }
    }
  }
}
