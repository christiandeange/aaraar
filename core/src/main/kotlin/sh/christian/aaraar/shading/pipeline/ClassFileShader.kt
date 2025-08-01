package sh.christian.aaraar.shading.pipeline

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import sh.christian.aaraar.shading.impl.transform.ClassRename
import sh.christian.aaraar.shading.impl.transform.JarProcessor
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Companion.EXT_CLASS
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.transform.PackageRemapper
import sh.christian.aaraar.shading.impl.transform.Transformable

internal class ClassFileShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun process(struct: Transformable): JarProcessor.Result {
    if (!struct.name.endsWith(EXT_CLASS)) return KEEP

    val classSource = ClassReader(struct.data)
    val classWriter = ClassWriter(classSource, 0)
    val visitor = ClassRemapper(classWriter, packageRemapper)

    classSource.accept(visitor, 0)

    struct.name = struct.name.removeSuffix(EXT_CLASS).let(packageRemapper::mapType) + EXT_CLASS
    struct.data = classWriter.toByteArray()

    return KEEP
  }
}
