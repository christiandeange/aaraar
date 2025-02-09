package sh.christian.aaraar.shading.pipeline

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.config.ClassDelete
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.DISCARD
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP
import com.tonicsystems.jarjar.util.ClassNameUtils
import com.tonicsystems.jarjar.util.ClassNameUtils.EXT_CLASS

internal class ClassFileFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    if (classDeletePatterns.isEmpty() || !ClassNameUtils.isClass(struct.name)) return KEEP

    return if (shouldDeletePath(struct.name.removeSuffix(EXT_CLASS))) {
      DISCARD
    } else {
      KEEP
    }
  }

  private fun shouldDeletePath(className: String): Boolean {
    return classDeletePatterns.any { it.matches(className) }
  }
}
