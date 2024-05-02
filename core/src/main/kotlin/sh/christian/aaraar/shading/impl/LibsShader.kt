package sh.christian.aaraar.shading.impl

import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.Shader

/**
 * Standard implementation for shading the `libs` folder by delegating to another JAR shader implementation.
 */
class LibsShader(
  private val genericJarArchiveShader: Shader<GenericJarArchive>,
) : Shader<Libs> {
  override fun shade(source: Libs, shadeConfiguration: ShadeConfiguration): Libs {
    val shadedFiles = source.files.mapValues { (path, contents) ->
      if (path.substringAfterLast('.') == "jar") {
        GenericJarArchive.from(contents, keepMetaFiles = true)
          ?.let { genericJarArchiveShader.shade(it, shadeConfiguration) }
          ?.bytes()
          ?: contents
      } else {
        contents
      }
    }

    return Libs(FileSet.from(shadedFiles))
  }
}
