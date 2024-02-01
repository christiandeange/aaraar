package sh.christian.aaraar.utils

import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

internal operator fun Path.div(path: String): Path {
  return resolve(path)
}

internal operator fun FileSystem.div(path: String): Path {
  return getPath(path)
}

internal fun Path.mkdirs(): Path {
  return apply { parent?.let(Files::createDirectories) }
}

internal fun Path.deleteIfExists(): Path {
  return apply { Files.deleteIfExists(this) }
}

/**
 * Creates a new archive file at the specified path, returning a [FileSystem] that represents the internal structure
 * of the archive to which files can be read, written, and deleted.
 */
fun <T> Path.createArchive(block: (FileSystem) -> T): T = asArchiveFileSystem(env = mapOf("create" to true), block)

/**
 * Opens an existing archive file at the specified path, returning a [FileSystem] that represents the internal structure
 * of the archive to which files can be read, written, and deleted.
 */
fun <T> Path.openArchive(block: (FileSystem) -> T): T = asArchiveFileSystem(env = emptyMap(), block)

private fun <T> Path.asArchiveFileSystem(
  env: Map<String, Any?> = emptyMap(),
  block: (FileSystem) -> T,
): T {
  val fileSystemUri = URI.create("jar:file:${toAbsolutePath()}")
  return runCatching {
    FileSystems.newFileSystem(fileSystemUri, env)
  }.getOrElse { e ->
    throw IllegalStateException("Cannot create filesystem for ${toAbsolutePath()}", e)
  }.use(block)
}
