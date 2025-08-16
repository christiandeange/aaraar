package sh.christian.aaraar.shading.impl

import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.Shader
import sh.christian.aaraar.shading.impl.transform.JarProcessorChain
import sh.christian.aaraar.shading.pipeline.ClassFileFilter
import sh.christian.aaraar.shading.pipeline.ClassFileShader
import sh.christian.aaraar.shading.pipeline.ClassFilesProcessor
import sh.christian.aaraar.shading.pipeline.KotlinModuleFilter
import sh.christian.aaraar.shading.pipeline.KotlinModuleShader
import sh.christian.aaraar.shading.pipeline.ResourceFileShader
import sh.christian.aaraar.shading.pipeline.ResourceFilter
import sh.christian.aaraar.shading.pipeline.ServiceLoaderFilter
import sh.christian.aaraar.shading.pipeline.ServiceLoaderShader

/**
 * Standard implementation for shading a JAR file by applying rules from the [ShadeConfiguration] in this order:
 * - Remove class files matching [`classDeletes`][ShadeConfiguration.classDeletes].
 * - Remove resource files matching [`resourceExclusions`][ShadeConfiguration.resourceDeletes].
 * - Rename class files and class references matching [`classRenames`][ShadeConfiguration.classRenames].
 * - Rename resource files matching [`resourceRenames`][ShadeConfiguration.resourceRenames].
 *
 * This ordering is important since class files are removed based on their _original_ name, not their shaded name.
 */
class GenericJarArchiveShader : Shader<GenericJarArchive> {
  override fun shade(source: GenericJarArchive, shadeConfiguration: ShadeConfiguration): GenericJarArchive {
    val processor = JarProcessorChain(
      ResourceFilter(shadeConfiguration.resourceDeletes),
      ClassFileFilter(shadeConfiguration.classDeletes),
      ClassFileShader(shadeConfiguration.classRenames),
      ResourceFileShader(shadeConfiguration.resourceRenames),
      ServiceLoaderFilter(shadeConfiguration.classDeletes),
      ServiceLoaderShader(shadeConfiguration.classRenames),
      KotlinModuleFilter(shadeConfiguration.classDeletes),
      KotlinModuleShader(shadeConfiguration.classRenames),
    )

    val newArchiveEntries = ClassFilesProcessor(processor).process(source)

    return GenericJarArchive(newArchiveEntries)
  }
}
