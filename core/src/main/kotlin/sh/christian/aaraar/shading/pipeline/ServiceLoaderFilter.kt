package sh.christian.aaraar.shading.pipeline

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.config.ClassDelete
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.DISCARD
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP

internal class ServiceLoaderFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    if (classDeletePatterns.isEmpty() || !struct.name.startsWith("META-INF/services/")) return KEEP
    val originalFile = struct.data.decodeToString()

    val newContents = buildString {
      val line = StringBuilder()

      originalFile.forEach { c ->
        if (c == '\n' || c == '\r') {
          val className = line.toString()
          if (!shouldDeleteClass(className)) {
            append(className)
            append(c)
          }
          line.clear()
        } else {
          line.append(c)
        }
      }

      val className = line.toString()
      if (!shouldDeleteClass(className)) {
        append(className)
      }
    }

    return if (newContents.isBlank()) {
      DISCARD
    } else {
      struct.data = newContents.encodeToByteArray()
      KEEP
    }
  }

  private fun shouldDeleteClass(className: String): Boolean {
    return shouldDeletePath(className.replace('.', '/'))
  }

  private fun shouldDeletePath(className: String): Boolean {
    return classDeletePatterns.any { it.matches(className) }
  }
}
