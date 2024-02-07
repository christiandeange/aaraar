package sh.christian.aaraar.gradle

import sh.christian.aaraar.model.ShadeConfiguration
import java.io.File
import java.io.Serializable

/**
 * Defines the environment of all applicable shading rules.
 */
data class ShadeEnvironment(
  val rules: List<ShadeConfigurationRule>,
) : Serializable {

  companion object {
    private const val serialVersionUID = 1L
  }
}

/**
 * Defines an individual shading rule.
 *
 * Each rule declares the shading rename and deletion configurations, and the scope that those apply to.
 */
data class ShadeConfigurationRule(
  val scope: ShadeConfigurationScope,
  val configuration: ShadeConfiguration,
) : Serializable {
  companion object {
    private const val serialVersionUID = 1L
  }
}

/**
 * The scope of a shading rule.
 *
 * Rules can be applied universally or scoped to a particular set of projects or dependencies.
 */
sealed interface ShadeConfigurationScope : Serializable {
  /** Applies to all sources. */
  object All : ShadeConfigurationScope {
    private fun readResolve(): Any = All
  }

  /**
   * Applies to one or more external dependencies.
   *
   * @param group Required. The group ID of the dependency.
   * @param name Optional. The artifact name of the dependency, or `null` to match all artifact names.
   * @param version Optional. The version of the dependency, or `null` to match all versions.
   */
  data class DependencyScope(
    val group: String,
    val name: String?,
    val version: String?,
  ) : ShadeConfigurationScope

  /** Applies to a single internal project, identified by its canonical path. */
  data class ProjectScope(
    val path: String,
  ) : ShadeConfigurationScope

  /** Applies to one or more files, relative to the module's project folder. */
  data class FilesScope(
    val files: Set<File>,
  ) : ShadeConfigurationScope

  companion object {
    private const val serialVersionUID = 1L
  }
}
