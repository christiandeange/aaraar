package sh.christian.aaraar.utils

import java.io.File
import java.nio.file.FileSystem
import java.nio.file.Path

internal operator fun File.div(path: String): File {
  return resolve(path)
}

internal operator fun Path.div(path: String): Path {
  return resolve(path)
}

internal operator fun FileSystem.div(path: String): Path {
  return getPath(path)
}
