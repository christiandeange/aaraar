package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.jarjar.transform.Transformable
import sh.christian.aaraar.shading.impl.jarjar.transform.config.ClassDelete
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor.Result.DISCARD
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils.EXT_CLASS

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
