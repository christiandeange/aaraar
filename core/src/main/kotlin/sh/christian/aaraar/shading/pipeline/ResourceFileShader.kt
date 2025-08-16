package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.transform.JarProcessor
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Companion.EXT_CLASS
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.transform.PathRemapper
import sh.christian.aaraar.shading.impl.transform.ResourceRename
import sh.christian.aaraar.shading.impl.transform.Transformable

internal class ResourceFileShader(
  resourceRenames: Map<String, String>,
) : JarProcessor {
  private val pathRemapper = PathRemapper(
    resourceRenames.map { (pattern, result) -> ResourceRename(pattern, result) }
  )

  override fun process(struct: Transformable): JarProcessor.Result {
    if (struct.name.endsWith(EXT_CLASS)) return KEEP

    struct.name = pathRemapper.mapType(struct.name)

    return KEEP
  }
}
