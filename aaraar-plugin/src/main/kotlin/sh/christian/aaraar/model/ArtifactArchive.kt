package sh.christian.aaraar.model

import sh.christian.aaraar.utils.*
import java.nio.file.Path

sealed class ArtifactArchive {
  abstract fun shaded(
    packagesToShade: Map<String, String>,
    packagesToRemove: Set<String>,
  ): ArtifactArchive

  abstract fun writeTo(path: Path)

  class AarArchive(
    val androidManifest: AndroidManifest,
    val classes: Classes,
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
      packagesToShade: Map<String, String>,
      packagesToRemove: Set<String>,
    ): ArtifactArchive {
      return AarArchive(
        androidManifest = androidManifest,
        classes = classes.shaded(packagesToShade, packagesToRemove),
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

    operator fun plus(other: AarArchive): AarArchive {
      return AarArchive(
        androidManifest = androidManifest + other.androidManifest,
        classes = classes + other.classes,
        resources = resources + other.resources,
        rTxt = rTxt + other.rTxt,
        publicTxt = publicTxt + other.publicTxt,
        assets = assets + other.assets,
        libs = libs + other.libs,
        jni = jni + other.jni,
        proguard = proguard + other.proguard,
        lintRules = lintRules + other.lintRules,
      )
    }

    operator fun plus(other: JarArchive): AarArchive {
      return AarArchive(
        androidManifest = androidManifest,
        classes = classes + other.classes,
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

  class JarArchive(
    val classes: Classes,
  ) : ArtifactArchive() {
    override fun shaded(
      packagesToShade: Map<String, String>,
      packagesToRemove: Set<String>,
    ): ArtifactArchive {
      return JarArchive(classes.shaded(packagesToShade, packagesToRemove))
    }

    override fun writeTo(path: Path) {
      classes.writeTo(path)
    }

    operator fun plus(other: AarArchive): AarArchive {
      return AarArchive(
        androidManifest = other.androidManifest,
        classes = classes + other.classes,
        resources = other.resources,
        rTxt = other.rTxt,
        publicTxt = other.publicTxt,
        assets = other.assets,
        libs = other.libs,
        jni = other.jni,
        proguard = other.proguard,
        lintRules = other.lintRules,
      )
    }

    operator fun plus(other: JarArchive): JarArchive {
      return JarArchive(
        classes = classes + other.classes,
      )
    }
  }

  // These look repetitive and useless, but the smart casts actually cause us to end up
  // using the concrete methods declared above.
  operator fun plus(other: ArtifactArchive): ArtifactArchive = when (this) {
    is AarArchive -> when (other) {
      is AarArchive -> this + other
      is JarArchive -> this + other
    }

    is JarArchive -> when (other) {
      is AarArchive -> this + other
      is JarArchive -> this + other
    }
  }

  companion object {
    fun from(path: Path): ArtifactArchive {
      val extension = path.toFile().extension

      return when (extension) {
        "jar" -> {
          val classes = Classes.from(path)
          JarArchive(classes)
        }

        "aar" -> {
          path.toAbsolutePath().openJar { aarRoot ->
            val androidManifest = AndroidManifest.from(aarRoot.android_manifest)
            val classes = Classes.from(aarRoot.classes_jar)
            val resources = Resources.from(aarRoot.res)
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
