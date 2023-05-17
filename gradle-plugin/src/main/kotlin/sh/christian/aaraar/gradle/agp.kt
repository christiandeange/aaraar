package sh.christian.aaraar.gradle

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import sh.christian.aaraar.gradle.agp.Agp7
import sh.christian.aaraar.gradle.agp.Agp8
import sh.christian.aaraar.gradle.agp.AgpCompat

private const val AGP_8 = 8
private const val AGP_7 = 7

internal val Project.agp: AgpCompat
  get() {
    val agpVersion = extensions.getByType<LibraryAndroidComponentsExtension>().pluginVersion
    return when {
      agpVersion.major > AGP_8 -> Agp8(this).also {
        project.logger.warn("aaraar has not been tested against AGP > 8. Use at your own risk!")
      }
      agpVersion.major == AGP_8 -> Agp8(this)
      agpVersion.major == AGP_7 -> Agp7(this)
      else -> error("aaraar is not compatible with AGP < 7")
    }
  }
