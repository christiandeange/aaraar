package sh.christian.aaraar.shading.impl

import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.Shader

/**
 * Standard implementation for shading `classes.jar` by delegating to another JAR shader implementation.
 */
class ClassesShader(
  private val genericJarArchiveShader: Shader<GenericJarArchive>,
) : Shader<Classes> {
  override fun shade(source: Classes, shadeConfiguration: ShadeConfiguration): Classes {
    return Classes.from(genericJarArchiveShader.shade(source.archive, shadeConfiguration))
  }
}
