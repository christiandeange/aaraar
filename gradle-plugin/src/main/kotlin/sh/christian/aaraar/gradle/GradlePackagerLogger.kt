package sh.christian.aaraar.gradle

import org.gradle.api.logging.Logger
import sh.christian.aaraar.packaging.PackagerLogger

class GradlePackagerLogger(
  private val logger: Logger,
) : PackagerLogger {
  override fun info(message: String) {
    logger.info(message)
  }

  override fun warning(message: String) {
    logger.warn(message)
  }

  override fun warning(message: String, exception: Exception) {
    logger.warn(message, exception)
  }

  override fun error(message: String) {
    logger.error(message)
  }

  override fun error(message: String, exception: Exception) {
    logger.error(message, exception)
  }
}
