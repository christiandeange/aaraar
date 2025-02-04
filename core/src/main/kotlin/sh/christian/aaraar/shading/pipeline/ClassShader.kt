package sh.christian.aaraar.shading.pipeline

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.asm.PackageRemapper
import com.tonicsystems.jarjar.transform.config.ClassRename
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.KEEP
import com.tonicsystems.jarjar.util.ClassNameUtils
import com.tonicsystems.jarjar.util.ClassNameUtils.EXT_CLASS
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper

internal class ClassShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun scan(struct: Transformable): JarProcessor.Result = KEEP

  override fun process(struct: Transformable): JarProcessor.Result {
    if (ClassNameUtils.isClass(struct.name)) {
      shadeClass(struct)
    } else if (struct.name.startsWith("META-INF/services/")) {
      shadeServiceLoader(struct)
    }

    return KEEP
  }

  private fun shadeClass(struct: Transformable) {
    val classSource = ClassReader(struct.data)
    val classWriter = ClassWriter(classSource, 0)
    val visitor = ClassRemapper(classWriter, packageRemapper)

    classSource.accept(visitor, 0)

    struct.name = packageRemapper.mapType(struct.name.replace(EXT_CLASS, "")) + EXT_CLASS
    struct.data = classWriter.toByteArray()
  }

  private fun shadeServiceLoader(struct: Transformable) {
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
  }
}
