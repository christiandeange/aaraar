package sh.christian.aaraar.utils

import java.io.File

internal operator fun File.div(path: String): File {
  return resolve(path)
}
