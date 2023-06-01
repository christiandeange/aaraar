package sh.christian.aaraar.gradle.agp

import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
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
   * Access to the variant's compile [Configuration].
   * The returned [Configuration] should not be resolved until execution time.
   */
  val compileConfiguration: Configuration

  /**
   * Access to the variant's runtime [Configuration].
   * The returned [Configuration] should not be resolved until execution time.
   */
  val runtimeConfiguration: Configuration

  /**
   * Register a transformation of the AAR produced by this variant.
   * [inputAar] is set to the input, and the transformed AAR should be written to [outputAar].
   */
  fun <T : Task> registerAarTransform(
    task: TaskProvider<T>,
    inputAar: (T) -> RegularFileProperty,
    outputAar: (T) -> RegularFileProperty,
  )

  fun name(
    prefix: String = "",
    suffix: String = "",
  ): String {
    return if (prefix.isEmpty()) {
      variantName + suffix
    } else if (prefix.last().isLetterOrDigit()) {
      prefix + variantName.capitalize() + suffix
    } else {
      prefix + variantName + suffix
    }
  }
}
