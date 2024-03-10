package sh.christian.aaraar.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.setProperty

/**
 * Configures the scopes and rules for shading class files.
 *
 * Rules can be applied universally or scoped to particular external or project dependencies.
 */
class AarAarShading(
  private val objects: ObjectFactory,
  private val dependencies: DependencyHandler,
) {
  internal val configurations: SetProperty<ScopedShadeConfiguration> =
    objects.setProperty<ScopedShadeConfiguration>().convention(mutableSetOf())

  fun createRule(configure: Action<in ScopedShadeConfiguration>) {
    createRule(emptySet(), configure)
  }

  fun createRule(
    vararg scopes: ShadeConfigurationScope,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    createRule(scopes.toSet(), configure)
  }

  fun createRule(
    scopes: Collection<ShadeConfigurationScope>,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    val resolvedScope = when (scopes.size) {
      0 -> ShadeConfigurationScope.All
      1 -> scopes.single()
      else -> ShadeConfigurationScope.AnyScope(scopes.toSet())
    }

    configurations.add(
      ScopedShadeConfiguration(resolvedScope, objects)
        .also { configure(it) }
    )
  }

  /**
   * Add shading rules that apply to all sources.
   */
  fun all(): ShadeConfigurationScope {
    return ShadeConfigurationScope.All
  }

  /**
   * Add shading rules that apply to dependencies from a particular group.
   * Any name and any version in this artifact group will inherit these rules.
   */
  fun forGroup(group: String): ShadeConfigurationScope {
    return ShadeConfigurationScope.DependencyScope(group, null, null)
  }

  /**
   * Add shading rules that apply to dependencies from a particular group and name.
   * Any version of this artifact group and name will inherit these rules.
   *
   * Dependencies are evaluated as per [DependencyHandler.create].
   */
  fun forModule(dependency: String): ShadeConfigurationScope {
    return dependencies.create(dependency).let {
      ShadeConfigurationScope.DependencyScope(it.group.orEmpty(), it.name, null)
    }
  }

  /**
   * Add shading rules that apply to a dependency with a particular group, name, and version.
   * **Only** this version of this artifact group and name will inherit these rules.
   *
   * Dependencies are evaluated as per [DependencyHandler.create].
   */
  fun forDependency(dependency: Any): ShadeConfigurationScope {
    return dependencies.create(dependency).let {
      ShadeConfigurationScope.DependencyScope(it.group.orEmpty(), it.name, it.version)
    }
  }

  /**
   * Add shading rules that apply only to a particular project.
   */
  fun forProject(path: String): ShadeConfigurationScope {
    return ShadeConfigurationScope.ProjectScope(path)
  }

  /**
   * Add shading rules that apply to one or more files, relative to the module's project folder.
   *
   * Paths are evaluated as per [Project.files].
   */
  fun forFiles(files: Any): ShadeConfigurationScope {
    return ShadeConfigurationScope.FilesScope(objects.fileCollection().from(files).files)
  }
}
