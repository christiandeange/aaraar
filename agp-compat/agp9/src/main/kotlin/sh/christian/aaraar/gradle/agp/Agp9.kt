package sh.christian.aaraar.gradle.agp

import com.android.build.api.attributes.BuildTypeAttr
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

/**
 * Compatibility layer for using AGP 9 at runtime.
 */
class Agp9(private val project: Project) : AgpCompat {
  private val androidComponents = project.extensions.getByType<LibraryAndroidComponentsExtension>()

  override val android: AndroidExtension =
    Agp9AndroidExtension(project.extensions.getByType<LibraryExtension>())

  override fun AttributeContainer.buildTypeAttribute(buildType: String) {
    attribute(BuildTypeAttr.ATTRIBUTE, project.objects.named(buildType))
  }

  override fun onVariants(callback: (AndroidVariant) -> Unit) {
    return androidComponents.onVariants { callback(Agp9AndroidVariant(it)) }
  }
}
