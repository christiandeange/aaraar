package sh.christian.aaraar.shading.pipeline

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import sh.christian.aaraar.shading.impl.jarjar.transform.Transformable
import sh.christian.aaraar.shading.impl.jarjar.transform.asm.PackageRemapper
import sh.christian.aaraar.shading.impl.jarjar.transform.config.ClassRename
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils.EXT_CLASS

internal class ClassFileShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun scan(struct: Transformable): JarProcessor.Result = KEEP

  override fun process(struct: Transformable): JarProcessor.Result {
    if (!ClassNameUtils.isClass(struct.name)) return KEEP

    val classSource = ClassReader(struct.data)
    val classWriter = ClassWriter(classSource, 0)
    val visitor = ClassRemapper(classWriter, packageRemapper)

    classSource.accept(visitor, 0)

    struct.name = packageRemapper.mapType(struct.name.replace(EXT_CLASS, "")) + EXT_CLASS
    struct.data = classWriter.toByteArray()

    return KEEP
  }
}
