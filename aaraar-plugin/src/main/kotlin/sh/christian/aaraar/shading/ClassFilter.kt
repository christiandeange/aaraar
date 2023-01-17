package sh.christian.aaraar.shading

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.config.ClassDelete
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.DISCARD
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP
import com.tonicsystems.jarjar.util.ClassNameUtils
import com.tonicsystems.jarjar.util.ClassNameUtils.EXT_CLASS
import com.tonicsystems.jarjar.util.ClassNameUtils.pathToJavaName

class ClassFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    return when {
      !ClassNameUtils.isClass(struct.name) -> KEEP
      classDeletePatterns.isEmpty() -> KEEP
      classDeletePatterns.none { it.matches(struct.name.replace(EXT_CLASS, "")) } -> KEEP
      else -> DISCARD
    }
  }
}
