package sh.christian.aaraar.gradle.agp

import com.android.build.api.dsl.LibraryExtension

internal class Agp7AndroidExtension(
  private val android: LibraryExtension,
) : AndroidExtension {
  override fun onBuildTypes(callback: (String) -> Unit) {
    return android.buildTypes.configureEach { callback(name) }
  }
}
