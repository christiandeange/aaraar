package sh.christian.aaraar.shading.impl

import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.Shader

/**
 * Standard implementation for shading a JAR artifact by shading the JAR directly.
 */
class JarArchiveShader(
  private val classesShader: Shader<Classes>,
) : Shader<JarArchive> {
  override fun shade(source: JarArchive, shadeConfiguration: ShadeConfiguration): JarArchive {
    return JarArchive(classesShader.shade(source.classes, shadeConfiguration))
  }
}
