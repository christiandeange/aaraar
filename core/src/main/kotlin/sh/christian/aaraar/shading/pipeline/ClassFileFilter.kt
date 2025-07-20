package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.transform.ClassDelete
import sh.christian.aaraar.shading.impl.transform.JarProcessor
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Companion.EXT_CLASS
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.DISCARD
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.transform.Transformable

internal class ClassFileFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun process(struct: Transformable): JarProcessor.Result {
    if (classDeletePatterns.isEmpty() || !struct.name.endsWith(EXT_CLASS)) return KEEP

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
