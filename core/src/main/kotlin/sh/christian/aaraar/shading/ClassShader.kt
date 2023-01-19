package sh.christian.aaraar.shading

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

class ClassShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun scan(struct: Transformable): JarProcessor.Result = KEEP

  override fun process(struct: Transformable): JarProcessor.Result {
    if (ClassNameUtils.isClass(struct.name)) {
      val classSource = ClassReader(struct.data)
      val classWriter = ClassWriter(classSource, 0)
      val visitor = ClassRemapper(classWriter, packageRemapper)

      classSource.accept(visitor, 0)

      struct.name = packageRemapper.mapType(struct.name.replace(EXT_CLASS, "")) + EXT_CLASS
      struct.data = classWriter.toByteArray()
    }

    return KEEP
  }
}
