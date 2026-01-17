package sh.christian.aaraar.packaging

/**
 * Simple logger interface for the [Packager].
 */
interface PackagerLogger {
  fun info(message: String)

  fun warning(message: String)

  fun warning(message: String, exception: Exception)

  fun error(message: String)

  fun error(message: String, exception: Exception)
}
