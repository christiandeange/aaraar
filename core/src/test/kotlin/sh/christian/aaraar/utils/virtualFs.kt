package sh.christian.aaraar.utils

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class VirtualOutputContext : Closeable {
  private val fileSystem = Jimfs.newFileSystem(Configuration.unix())

  val root = fileSystem.rootDirectories.first()

  fun withFile(block: VirtualOutputFileContext.() -> Unit) {
    val tempFile = Files.createTempFile(root, "test-file", ".tmp")
    VirtualOutputFileContext(tempFile).apply(block)
  }

  fun withDirectory(block: VirtualOutputDirectoryContext.() -> Unit) {
    val tempDirectory = Files.createTempDirectory(root, "test-dir")
    VirtualOutputDirectoryContext(tempDirectory).apply(block)
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
  fun files(): List<Path> {
    return Files.walk(root).toList()
  }
}

inline fun assert(block: VirtualOutputContext.() -> Unit) {
  VirtualOutputContext().use {
    it.block()
  }
}

fun withFile(block: VirtualOutputFileContext.() -> Unit) = assert { withFile(block) }

fun withDirectory(block: VirtualOutputDirectoryContext.() -> Unit) = assert { withDirectory(block) }
