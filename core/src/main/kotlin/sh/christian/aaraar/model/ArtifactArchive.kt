package sh.christian.aaraar.model

import sh.christian.aaraar.Environment
import sh.christian.aaraar.utils.aarMetadataProperties
import sh.christian.aaraar.utils.androidManifestXml
import sh.christian.aaraar.utils.apiJar
import sh.christian.aaraar.utils.assets
import sh.christian.aaraar.utils.clasesJar
import sh.christian.aaraar.utils.createArchive
import sh.christian.aaraar.utils.jni
import sh.christian.aaraar.utils.libs
import sh.christian.aaraar.utils.lintJar
import sh.christian.aaraar.utils.mkdirs
import sh.christian.aaraar.utils.navigationJson
import sh.christian.aaraar.utils.openArchive
import sh.christian.aaraar.utils.proguardTxt
import sh.christian.aaraar.utils.publicTxt
import sh.christian.aaraar.utils.rTxt
import sh.christian.aaraar.utils.res
import java.nio.file.Path

/**
 * The base class of a packaged archive output, typically either a `jar` or `aar` file.
 */
sealed class ArtifactArchive {
  abstract val classes: Classes

  abstract fun writeTo(path: Path)

  companion object {
    fun from(
      path: Path,
      environment: Environment,
    ): ArtifactArchive {
      return when (path.toFile().extension) {
        "jar" -> JarArchive.from(path, environment)
        "aar" -> AarArchive.from(path, environment)
        else -> error("Unknown dependency type: $path")
      }
    }
  }
}

/**
 * Represents a packaged `jar` archive output.
 */
class JarArchive(
  override val classes: Classes,
) : ArtifactArchive() {
  override fun writeTo(path: Path) {
    classes.writeTo(path)
  }

  companion object {
    fun from(
      path: Path,
      environment: Environment,
    ): JarArchive = JarArchive(Classes.from(path, environment.keepClassesMetaFiles))
  }
}

/**
 * Represents a packaged `aar` archive output.
 */
class AarArchive(
  val aarMetadata: AarMetadata,
  val androidManifest: AndroidManifest,
  override val classes: Classes,
  val resources: Resources,
  val rTxt: RTxt,
  val publicTxt: PublicTxt,
  val assets: Assets,
  val libs: Libs,
  val jni: Jni,
  val proguard: Proguard,
  val lintRules: LintRules,
  val navigationJson: NavigationJson,
  val apiJar: ApiJar,
  /**
   * TODO no idea how /prefab folder works, add support for it later.
   */
) : ArtifactArchive() {
  override fun writeTo(path: Path) {
    path.createArchive { outputAar ->
      aarMetadata.writeTo(outputAar.aarMetadataProperties.apply { mkdirs() })
      androidManifest.writeTo(outputAar.androidManifestXml)
      classes.writeTo(outputAar.clasesJar)
      resources.writeTo(outputAar.res)
      rTxt.writeTo(outputAar.rTxt)
      publicTxt.writeTo(outputAar.publicTxt)
      assets.writeTo(outputAar.assets)
      libs.writeTo(outputAar.libs)
      jni.writeTo(outputAar.jni)
      proguard.writeTo(outputAar.proguardTxt)
      lintRules.writeTo(outputAar.lintJar)
      navigationJson.writeTo(outputAar.navigationJson)
      apiJar.writeTo(outputAar.apiJar)
    }
  }

  companion object {
    fun from(
      path: Path,
      environment: Environment,
    ): AarArchive = path.toAbsolutePath().openArchive { aarRoot ->
      val aarMetadata = AarMetadata.from(aarRoot.aarMetadataProperties)
      val androidManifest = AndroidManifest.from(aarRoot.androidManifestXml)
      val classes = Classes.from(aarRoot.clasesJar, environment.keepClassesMetaFiles)
      val resources = Resources.from(
        path = aarRoot.res,
        packageName = androidManifest.packageName,
        minSdk = androidManifest.minSdk,
        androidAaptIgnore = environment.androidAaptIgnore,
      )
      val rTxt = RTxt.from(aarRoot.rTxt, androidManifest.packageName)
      val publicTxt = PublicTxt.from(aarRoot.publicTxt, androidManifest.packageName)
      val assets = Assets.from(aarRoot.assets)
      val libs = Libs.from(aarRoot.libs)
      val jni = Jni.from(aarRoot.jni)
      val proguard = Proguard.from(aarRoot.proguardTxt)
      val lintRules = LintRules.from(aarRoot.lintJar)
      val navigationJson = NavigationJson.from(aarRoot.navigationJson)
      val apiJar = ApiJar.from(aarRoot.apiJar, environment.keepClassesMetaFiles)

      AarArchive(
        aarMetadata = aarMetadata,
        androidManifest = androidManifest,
        classes = classes,
        resources = resources,
        rTxt = rTxt,
        publicTxt = publicTxt,
        assets = assets,
        libs = libs,
        jni = jni,
        proguard = proguard,
        lintRules = lintRules,
        navigationJson = navigationJson,
        apiJar = apiJar,
      )
    }
  }
}
