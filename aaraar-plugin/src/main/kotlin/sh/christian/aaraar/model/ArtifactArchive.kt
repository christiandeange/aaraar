package sh.christian.aaraar.model

import sh.christian.aaraar.Environment
import sh.christian.aaraar.utils.android_manifest
import sh.christian.aaraar.utils.assets
import sh.christian.aaraar.utils.classes_jar
import sh.christian.aaraar.utils.createJar
import sh.christian.aaraar.utils.jni
import sh.christian.aaraar.utils.libs
import sh.christian.aaraar.utils.lint_jar
import sh.christian.aaraar.utils.openJar
import sh.christian.aaraar.utils.proguard_txt
import sh.christian.aaraar.utils.public_txt
import sh.christian.aaraar.utils.r_txt
import sh.christian.aaraar.utils.res
import java.nio.file.Path

sealed class ArtifactArchive {
  abstract val classes: Classes

  abstract fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): ArtifactArchive

  abstract fun writeTo(path: Path)

  companion object {
    fun from(
      path: Path,
      environment: Environment,
    ): ArtifactArchive {
      return when (path.toFile().extension) {
        "jar" -> {
          val classes = Classes.from(path, environment.keepClassesMetaFiles)
          JarArchive(classes)
        }

        "aar" -> {
          path.toAbsolutePath().openJar { aarRoot ->
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

            AarArchive(
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
            )
          }
        }

        else -> {
          error("Unknown dependency type: $path")
        }
      }
    }
  }
}

class JarArchive(
  override val classes: Classes,
) : ArtifactArchive() {
  override fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): ArtifactArchive {
    return JarArchive(classes.shaded(classRenames, classDeletes))
  }

  override fun writeTo(path: Path) {
    classes.writeTo(path)
  }
}

class AarArchive(
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
  /**
   * TODO
   * no idea how /prefab folder works, add support for it later.
   * api.jar is read by tooling, but no easy way to create it using APIs or standard tooling:
   * https://issuetracker.google.com/issues/64315897
   */
) : ArtifactArchive() {
  override fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): ArtifactArchive {
    return AarArchive(
      androidManifest = androidManifest,
      classes = classes.shaded(classRenames, classDeletes),
      resources = resources,
      rTxt = rTxt,
      publicTxt = publicTxt,
      assets = assets,
      libs = libs.shaded(classRenames, classDeletes),
      jni = jni,
      proguard = proguard,
      lintRules = lintRules,
    )
  }

  fun mergeWith(others: List<ArtifactArchive>): ArtifactArchive {
    val aars = others.filterIsInstance<AarArchive>()
    return AarArchive(
      androidManifest = androidManifest + aars.map { it.androidManifest },
      classes = classes + others.map { it.classes },
      resources = resources + aars.map { it.resources },
      rTxt = rTxt + aars.map { it.rTxt },
      publicTxt = publicTxt + aars.map { it.publicTxt },
      assets = assets + aars.map { it.assets },
      libs = libs + aars.map { it.libs },
      jni = jni + aars.map { it.jni },
      proguard = proguard + aars.map { it.proguard },
      lintRules = lintRules + aars.map { it.lintRules },
    )
  }

  override fun writeTo(path: Path) {
    path.createJar { outputAar ->
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
    }
  }
}
