package sh.christian.aaraar.shading.pipeline

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.config.ClassDelete
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.DISCARD
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP
import com.tonicsystems.jarjar.util.ClassNameUtils
import com.tonicsystems.jarjar.util.ClassNameUtils.EXT_CLASS

internal class ClassFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    return if (classDeletePatterns.isEmpty()) {
      KEEP
    } else if (ClassNameUtils.isClass(struct.name)) {
      processClass(struct)
    } else if (struct.name.startsWith("META-INF/services/")) {
      processServiceLoader(struct)
    } else {
      KEEP
    }
  }

  private fun processClass(struct: Transformable): JarProcessor.Result {
    return if (shouldDeletePath(struct.name.removeSuffix(EXT_CLASS))) {
      DISCARD
    } else {
      KEEP
    }
  }

  private fun processServiceLoader(struct: Transformable): JarProcessor.Result {
    val originalFile = struct.data.decodeToString()

    struct.data = buildString {
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
    }.encodeToByteArray()

    return if (struct.data.isEmpty()) {
      DISCARD
    } else {
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
