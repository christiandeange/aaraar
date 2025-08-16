package sh.christian.aaraar.shading.pipeline

import kotlinx.metadata.jvm.KotlinModuleMetadata
import kotlinx.metadata.jvm.UnstableMetadataApi
import sh.christian.aaraar.shading.impl.transform.ClassRename
import sh.christian.aaraar.shading.impl.transform.JarProcessor
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.transform.PackageRemapper
import sh.christian.aaraar.shading.impl.transform.Transformable

@OptIn(UnstableMetadataApi::class)
internal class KotlinModuleShader(
  classRenames: Map<String, String>,
) : JarProcessor {
  private val packageRemapper = PackageRemapper(
    classRenames.map { (pattern, result) -> ClassRename(pattern, result) }
  )

  override fun process(struct: Transformable): JarProcessor.Result {
    if (!struct.name.endsWith(".kotlin_module")) return KEEP

    val metadata = KotlinModuleMetadata.read(struct.data)
    val kotlinModule = metadata.kmModule

    val packageParts = kotlinModule.packageParts.toMap()
    kotlinModule.packageParts.clear()
    kotlinModule.packageParts.putAll(
      packageParts.map { (packageName, packageParts) ->
        val newPackageName = packageRemapper.mapPackage(packageName)

        val fileFacades = packageParts.fileFacades.toList()
        packageParts.fileFacades.clear()
        packageParts.fileFacades.addAll(fileFacades.map { packageRemapper.mapType(it) })

        newPackageName to packageParts
      }
    )
    struct.data = metadata.write()

    return KEEP
  }

  private fun PackageRemapper<ClassRename>.mapPackage(packageName: String): String {
    val classSuffix = ".Dummy"
    return mapValue(packageName + classSuffix).toString().removeSuffix(classSuffix)
  }
}
