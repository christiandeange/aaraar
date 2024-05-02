package sh.christian.aaraar.shading.pipeline

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.DISCARD
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP
import sh.christian.aaraar.utils.div
import java.nio.file.FileSystems

internal class ResourceFilter(
  private val resourceDeletes: Set<String>,
) : JarProcessor {
  private val fs = FileSystems.getDefault()

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    return when {
      resourceDeletes.isEmpty() -> KEEP
      resourceDeletes.none { fs.getPathMatcher("glob:$it").matches(fs / struct.name) } -> KEEP
      else -> DISCARD
    }
  }
}
