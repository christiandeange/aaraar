package sh.christian.aaraar.gradle.agp

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskProvider

/**
 * A facade of some of the interactions with Android module variants.
 */
interface AndroidVariant {
  /**
   * The name of the variant.
   */
  val variantName: String

  /**
   * The name of the variant's build type, if present.
   */
  val buildType: String?

  /**
   * The namespace of the variant for generated R and BuildConfig classes.
   */
  val namespace: String

  /**
   * The packaging options for handling resource merge conflicts from dependencies.
   */
  val packaging: AndroidPackaging

  /**
   * Register a transformation of the AAR produced by this variant.
   * [inputAar] is set to the input, and the transformed AAR should be written to [outputAar].
   */
  fun <T : Task> registerAarTransform(
    task: TaskProvider<T>,
    inputAar: (T) -> RegularFileProperty,
    outputAar: (T) -> RegularFileProperty,
  )

  /**
   * Returns a string using an optional prefix and suffix to surround the variant name, applying the default
   * snake-casing formatting convention that Gradle naming often follows.
   */
  fun name(
    prefix: String = "",
    suffix: String = "",
  ): String {
    return if (prefix.isEmpty()) {
      variantName + suffix
    } else if (prefix.last().isLetterOrDigit()) {
      @Suppress("DEPRECATION")
      prefix + variantName.capitalize() + suffix
    } else {
      prefix + variantName + suffix
    }
  }
}
