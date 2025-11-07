package sh.christian.aaraar.shading.impl

import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.Shader

/**
 * Standard implementation for shading an AAR artifact by shading `classes.jar` and all JAR files in the `libs` folder.
 */
class AarArchiveShader(
  private val classesShader: Shader<Classes>,
  private val libsShader: Shader<Libs>,
) : Shader<AarArchive> {
  override fun shade(source: AarArchive, shadeConfiguration: ShadeConfiguration): AarArchive {
    return AarArchive(
      aarMetadata = source.aarMetadata,
      androidManifest = source.androidManifest,
      classes = classesShader.shade(source.classes, shadeConfiguration),
      resources = source.resources,
      rTxt = source.rTxt,
      publicTxt = source.publicTxt,
      assets = source.assets,
      libs = libsShader.shade(source.libs, shadeConfiguration),
      jni = source.jni,
      proguard = source.proguard,
      lintRules = source.lintRules,
      navigationJson = source.navigationJson,
      apiJar = source.apiJar,
      prefab = source.prefab,
    )
  }
}
