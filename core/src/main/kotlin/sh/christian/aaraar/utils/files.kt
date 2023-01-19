package sh.christian.aaraar.utils

import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

operator fun File.div(path: String): File {
  return resolve(path)
}

operator fun Path.div(path: String): Path {
  return resolve(path)
}

operator fun FileSystem.div(path: String): Path {
  return getPath(path)
}

fun Path.mkdirs(): Path {
  return apply { parent?.let(Files::createDirectories) }
}

fun Path.deleteIfExists(): Path {
  return apply { Files.deleteIfExists(this) }
}

fun <T> Path.createJar(block: (FileSystem) -> T): T =
  asJarFileSystem(env = mapOf("create" to true), block)

fun <T> Path.openJar(block: (FileSystem) -> T): T =
  asJarFileSystem(env = emptyMap(), block)

private fun <T> Path.asJarFileSystem(
  env: Map<String, Any?> = emptyMap(),
  block: (FileSystem) -> T,
): T {
  val fileSystemUri = URI.create("jar:file:${toAbsolutePath()}")
  return runCatching {
    FileSystems.newFileSystem(fileSystemUri, env).use(block)
  }.getOrElse { e ->
    throw IllegalStateException("Cannot create filesystem for ${toAbsolutePath()}", e)
  }
}
