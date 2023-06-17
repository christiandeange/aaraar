package sh.christian.aaraar.utils

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class VirtualOutputContext : Closeable {
  private val fileSystem = Jimfs.newFileSystem(Configuration.unix())

  val root: Path = fileSystem.rootDirectories.first()

  fun withFile(block: VirtualOutputFileContext.() -> Unit): Path {
    val tempFile = Files.createTempFile(root, "test-file", ".tmp")
    VirtualOutputFileContext(tempFile).apply(block)
    return tempFile
  }

  fun withDirectory(block: VirtualOutputDirectoryContext.() -> Unit): Path {
    val tempDirectory = Files.createTempDirectory(root, "test-dir")
    VirtualOutputDirectoryContext(tempDirectory).apply(block)
    return tempDirectory
  }

  override fun close() {
    fileSystem.close()
  }
}

class VirtualOutputFileContext(val filePath: Path) {
  fun bytes(): ByteArray {
    return Files.readAllBytes(filePath)
  }

  fun string(): String {
    return Files.readString(filePath)
  }
}

class VirtualOutputDirectoryContext(val root: Path) {
  fun filePaths(): List<String> {
    return Files.walk(root)
      .asSequence()
      .filter(Files::isRegularFile)
      .map { it.toString() }
      .toList()
  }

  fun files(): Map<String, String> {
    return Files.walk(root)
      .asSequence()
      .filter(Files::isRegularFile)
      .associate { root.relativize(it).toString() to Files.readString(it) }
  }
}

inline fun withFileSystem(block: VirtualOutputContext.() -> Unit) {
  VirtualOutputContext().use {
    it.block()
  }
}

fun withFile(block: VirtualOutputFileContext.() -> Unit) = withFileSystem { withFile(block) }

fun withDirectory(block: VirtualOutputDirectoryContext.() -> Unit) = withFileSystem { withDirectory(block) }
