package sh.christian.aaraar.shading.impl

import com.tonicsystems.jarjar.transform.jar.JarProcessorChain
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.Shader
import sh.christian.aaraar.shading.pipeline.ClassFilesProcessor
import sh.christian.aaraar.shading.pipeline.ClassFilter
import sh.christian.aaraar.shading.pipeline.ClassShader
import sh.christian.aaraar.shading.pipeline.KotlinModuleFilter
import sh.christian.aaraar.shading.pipeline.KotlinModuleShader
import sh.christian.aaraar.shading.pipeline.ResourceFilter

/**
 * Standard implementation for shading a JAR file by applying rules from the [ShadeConfiguration] in this order:
 * - Remove resources matching [`resourceExclusions`][ShadeConfiguration.resourceExclusions].
 * - Remove class files matching [`classDeletes`][ShadeConfiguration.classDeletes].
 * - Rename class files and class references matching [`classRenames`][ShadeConfiguration.classRenames].
 *
 * This ordering is important since class files are removed based on their _original_ name, not their shaded name.
 */
class GenericJarArchiveShader : Shader<GenericJarArchive> {
  override fun shade(source: GenericJarArchive, shadeConfiguration: ShadeConfiguration): GenericJarArchive {
    val processor = JarProcessorChain().apply {
      add(ResourceFilter(shadeConfiguration.resourceExclusions))
      add(ClassFilter(shadeConfiguration.classDeletes))
      add(ClassShader(shadeConfiguration.classRenames))
      add(KotlinModuleFilter(shadeConfiguration.classDeletes))
      add(KotlinModuleShader(shadeConfiguration.classRenames))
    }

    val newArchiveEntries = ClassFilesProcessor(processor).process(source)

    return GenericJarArchive(newArchiveEntries)
  }
}
