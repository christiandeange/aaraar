package sh.christian.aaraar.shading.pipeline

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.asm.PackageRemapper
import com.tonicsystems.jarjar.transform.config.ClassRename
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP

internal class ServiceLoaderShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun scan(struct: Transformable): JarProcessor.Result = KEEP

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
