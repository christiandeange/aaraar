package sh.christian.aaraar.shading

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.DISCARD
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP

object DirectoryFilter : JarProcessor {
  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    return if (struct.name.endsWith("/")) DISCARD else KEEP
  }
}
