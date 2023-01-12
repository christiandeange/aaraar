package sh.christian.aaraar.model

import sh.christian.aaraar.utils.div
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path

sealed class ArtifactArchive {
  class AarArchive(
    val androidManifest: AndroidManifest,
    val classes: Classes?,
    val resources: Resources?,
    val rTxt: RTxt?,
    val publicTxt: PublicTxt?,
    val assets: Assets?,
    val libs: Libs?,
    val jni: Jni?,
    val proguard: Proguard?,
    val lintRules: LintRules?,
    /**
     * TODO
     * no idea how /prefab folder works, add support for it later.
     * api.jar is read by tooling, but no easy way to create it using APIs or standard tooling:
     * https://issuetracker.google.com/issues/64315897
     */
  ) : ArtifactArchive()

  class JarArchive(
    val classes: Classes,
  ) : ArtifactArchive()

  companion object {
    fun from(path: Path): ArtifactArchive {
      val extension = path.toFile().extension

      return when (extension) {
        "jar" -> {
          val classes = Classes.from(path)!!
          JarArchive(classes)
        }

        "aar" -> {
          val fileSystemUri = URI.create("jar:file:${path.toAbsolutePath()}")
          val fileSystemEnv = mapOf<String, Any?>()

          FileSystems.newFileSystem(fileSystemUri, fileSystemEnv).use { aarRoot ->
            // https://developer.android.com/studio/projects/android-library.html#aar-contents
            val androidManifest = AndroidManifest.from(aarRoot / "AndroidManifest.xml")
            val classes = Classes.from(aarRoot / "classes.jar")
            val resources = Resources.from(aarRoot / "res")
            val rTxt = RTxt.from(aarRoot / "R.txt", androidManifest.packageName)
            val publicTxt = PublicTxt.from(aarRoot / "public.txt", androidManifest.packageName)
            val assets = Assets.from(aarRoot / "assets")
            val libs = Libs.from(aarRoot / "libs")
            val jni = Jni.from(aarRoot / "jni")
            val proguard = Proguard.from(aarRoot / "proguard.txt")
            val lintRules = LintRules.from(aarRoot / "lint.jar")

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
