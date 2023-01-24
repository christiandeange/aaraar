package sh.christian.aaraar.model

import com.tonicsystems.jarjar.transform.jar.JarProcessorChain
import sh.christian.aaraar.shading.ClassFilesProcessor
import sh.christian.aaraar.shading.ClassFilter
import sh.christian.aaraar.shading.ClassShader
import sh.christian.aaraar.utils.createJar
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.mkdirs
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.jar.JarInputStream
import kotlin.streams.asSequence

class GenericJarArchive
private constructor(
  private val archiveEntries: Map<String, ByteArray>,
) : Mergeable<GenericJarArchive>, Map<String, ByteArray> by archiveEntries {

  fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): GenericJarArchive {
    val processor = JarProcessorChain().apply {
      add(ClassFilter(classDeletes))
      add(ClassShader(classRenames))
    }

    val newArchiveEntries = ClassFilesProcessor(processor).process(archiveEntries)

    return GenericJarArchive(newArchiveEntries)
  }

  override operator fun plus(others: List<GenericJarArchive>): GenericJarArchive {
    return GenericJarArchive(mergeContents(this, others))
  }

  fun bytes(): ByteArray {
    val tempJarFile = Files.createTempFile("out", ".jar").deleteIfExists()
    writeTo(tempJarFile)
    return Files.readAllBytes(tempJarFile).also {
      tempJarFile.deleteIfExists()
    }
  }

  fun writeTo(path: Path) {
    if (isEmpty()) {
      Files.deleteIfExists(path)
      return
    }

    val tempClassesJar = Files.createTempFile("classes", ".jar").deleteIfExists()

    tempClassesJar.createJar { classesJar ->
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

    private fun from(
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
