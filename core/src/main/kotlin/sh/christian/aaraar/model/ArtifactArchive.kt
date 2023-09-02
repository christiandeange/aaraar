package sh.christian.aaraar.model

import sh.christian.aaraar.Environment
import sh.christian.aaraar.utils.aar_metadata
import sh.christian.aaraar.utils.android_manifest
import sh.christian.aaraar.utils.assets
import sh.christian.aaraar.utils.classes_jar
import sh.christian.aaraar.utils.createJar
import sh.christian.aaraar.utils.jni
import sh.christian.aaraar.utils.libs
import sh.christian.aaraar.utils.lint_jar
import sh.christian.aaraar.utils.mkdirs
import sh.christian.aaraar.utils.navigation_json
import sh.christian.aaraar.utils.openJar
import sh.christian.aaraar.utils.proguard_txt
import sh.christian.aaraar.utils.public_txt
import sh.christian.aaraar.utils.r_txt
import sh.christian.aaraar.utils.res
import java.nio.file.Path

/**
 * The base class of a packaged archive output, typically either a `jar` or `aar` file.
 */
sealed class ArtifactArchive {
  abstract val classes: Classes

  abstract fun shaded(shadeConfiguration: ShadeConfiguration): ArtifactArchive

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
  override fun shaded(shadeConfiguration: ShadeConfiguration): ArtifactArchive {
    return JarArchive(classes.shaded(shadeConfiguration))
  }

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
  /**
   * TODO
   * no idea how /prefab folder works, add support for it later.
   * api.jar is read by tooling, but no easy way to create it using APIs or standard tooling:
   * https://issuetracker.google.com/issues/64315897
   */
) : ArtifactArchive() {
  override fun shaded(shadeConfiguration: ShadeConfiguration): ArtifactArchive {
    return AarArchive(
      aarMetadata = aarMetadata,
      androidManifest = androidManifest,
      classes = classes.shaded(shadeConfiguration),
      resources = resources,
      rTxt = rTxt,
      publicTxt = publicTxt,
      assets = assets,
      libs = libs.shaded(shadeConfiguration),
      jni = jni,
      proguard = proguard,
      lintRules = lintRules,
      navigationJson = navigationJson,
    )
  }

  override fun writeTo(path: Path) {
    path.createJar { outputAar ->
      aarMetadata.writeTo(outputAar.aar_metadata.apply { mkdirs() })
      androidManifest.writeTo(outputAar.android_manifest)
      classes.writeTo(outputAar.classes_jar)
      resources.writeTo(outputAar.res)
      rTxt.writeTo(outputAar.r_txt)
      publicTxt.writeTo(outputAar.public_txt)
      assets.writeTo(outputAar.assets)
      libs.writeTo(outputAar.libs)
      jni.writeTo(outputAar.jni)
      proguard.writeTo(outputAar.proguard_txt)
      lintRules.writeTo(outputAar.lint_jar)
      navigationJson.writeTo(outputAar.navigation_json)
    }
  }

  companion object {
    fun from(
      path: Path,
      environment: Environment,
    ): AarArchive = path.toAbsolutePath().openJar { aarRoot ->
      val aarMetadata = AarMetadata.from(aarRoot.aar_metadata)
      val androidManifest = AndroidManifest.from(aarRoot.android_manifest)
      val classes = Classes.from(aarRoot.classes_jar, environment.keepClassesMetaFiles)
      val resources = Resources.from(
        aarRoot.res,
        androidManifest.packageName,
        androidManifest.minSdk,
        environment.androidAaptIgnore,
      )
      val rTxt = RTxt.from(aarRoot.r_txt, androidManifest.packageName)
      val publicTxt = PublicTxt.from(aarRoot.public_txt, androidManifest.packageName)
      val assets = Assets.from(aarRoot.assets)
      val libs = Libs.from(aarRoot.libs)
      val jni = Jni.from(aarRoot.jni)
      val proguard = Proguard.from(aarRoot.proguard_txt)
      val lintRules = LintRules.from(aarRoot.lint_jar)
      val navigationJson = NavigationJson.from(aarRoot.navigation_json)

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
      )
    }
  }
}
