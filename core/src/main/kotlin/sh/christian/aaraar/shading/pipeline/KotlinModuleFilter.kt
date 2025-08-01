package sh.christian.aaraar.shading.pipeline

import kotlinx.metadata.jvm.KotlinModuleMetadata
import kotlinx.metadata.jvm.UnstableMetadataApi
import sh.christian.aaraar.shading.impl.transform.ClassDelete
import sh.christian.aaraar.shading.impl.transform.JarProcessor
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.DISCARD
import sh.christian.aaraar.shading.impl.transform.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.transform.Transformable

@OptIn(UnstableMetadataApi::class)
internal class KotlinModuleFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun process(struct: Transformable): JarProcessor.Result {
    if (!struct.name.endsWith(".kotlin_module") || classDeletePatterns.isEmpty()) return KEEP

    val metadata = KotlinModuleMetadata.read(struct.data)
    val kotlinModule = metadata.kmModule

    val packageParts = kotlinModule.packageParts.toMap()
    kotlinModule.packageParts.clear()
    kotlinModule.packageParts.putAll(
      packageParts.mapNotNull { (packageName, packageParts) ->
        val fileFacades = packageParts.fileFacades.toList()
        packageParts.fileFacades.clear()
        packageParts.fileFacades.addAll(
          fileFacades.filter { clazz -> classDeletePatterns.none { it.matches(clazz) } }
        )

        (packageName to packageParts).takeIf { packageParts.fileFacades.isNotEmpty() }
      }
    )

    return if (kotlinModule.packageParts.isNotEmpty()) {
      struct.data = metadata.write()
      KEEP
    } else {
      DISCARD
    }
  }
}
