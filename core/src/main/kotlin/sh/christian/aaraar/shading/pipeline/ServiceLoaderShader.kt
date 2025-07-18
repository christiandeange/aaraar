package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.transform.Transformable
import sh.christian.aaraar.shading.impl.transform.asm.PackageRemapper
import sh.christian.aaraar.shading.impl.transform.config.ClassRename
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor.Result.KEEP

internal class ServiceLoaderShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun process(struct: Transformable): JarProcessor.Result {
    if (!struct.name.startsWith("META-INF/services/")) return KEEP

    val originalFile = struct.data.decodeToString()

    struct.data = buildString {
      val line = StringBuilder()

      originalFile.forEach { c ->
        if (c == '\n' || c == '\r') {
          append(packageRemapper.mapValue(line.toString()))
          append(c)
          line.clear()
        } else {
          line.append(c)
        }
      }

      append(packageRemapper.mapValue(line.toString()))
    }.encodeToByteArray()

    return KEEP
  }
}
