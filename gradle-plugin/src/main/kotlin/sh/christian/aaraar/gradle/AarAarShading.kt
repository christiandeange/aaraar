package sh.christian.aaraar.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.setProperty
import sh.christian.aaraar.gradle.ScopeSelector.All
import sh.christian.aaraar.gradle.ScopeSelector.ForDependency
import sh.christian.aaraar.gradle.ScopeSelector.ForFiles
import sh.christian.aaraar.gradle.ScopeSelector.ForGroup
import sh.christian.aaraar.gradle.ScopeSelector.ForModule
import sh.christian.aaraar.gradle.ScopeSelector.ForProject

/**
 * Configures the scopes and rules for shading class files.
 *
 * Rules can be applied universally or scoped to particular external or project dependencies.
 */
class AarAarShading(
  private val objects: ObjectFactory,
) {
  internal val allConfiguration = ScopedShadeConfiguration(All, objects)

  internal val configurations: SetProperty<ScopedShadeConfiguration> =
    objects.setProperty<ScopedShadeConfiguration>().convention(mutableSetOf())

  /**
   * Add shading rules that apply to all sources.
   */
  fun all(configure: Action<in ScopedShadeConfiguration>) {
    configure(allConfiguration)
  }

  /**
   * Add shading rules that apply to dependencies from a particular group.
   * Any name and any version in this artifact group will inherit these rules.
   */
  fun forGroup(
    group: String,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    configurations.add(
      ScopedShadeConfiguration(ForGroup(group), objects)
        .also { configure(it) }
    )
  }

  /**
   * Add shading rules that apply to dependencies from a particular group and name.
   * Any version of this artifact group and name will inherit these rules.
   *
   * Dependencies are evaluated as per [DependencyHandler.create].
   */
  fun forModule(
    dependency: Any,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    configurations.add(
      ScopedShadeConfiguration(ForModule(dependency), objects)
        .also { configure(it) }
    )
  }

  /**
   * Add shading rules that apply to a dependency with a particular group, name, and version.
   * **Only** this version of this artifact group and name will inherit these rules.
   *
   * Dependencies are evaluated as per [DependencyHandler.create].
   */
  fun forDependency(
    dependency: Any,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    configurations.add(
      ScopedShadeConfiguration(ForDependency(dependency), objects)
        .also { configure(it) }
    )
  }

  /**
   * Add shading rules that apply only to a particular project.
   */
  fun forProject(
    project: Project,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    configurations.add(
      ScopedShadeConfiguration(ForProject(project.path), objects)
        .also { configure(it) }
    )
  }

  /**
   * Add shading rules that apply to one or more files, relative to the module's project folder.
   *
   * Paths are evaluated as per [Project.files].
   */
  fun forFiles(
    files: Any,
    configure: Action<in ScopedShadeConfiguration>,
  ) {
    configurations.add(
      ScopedShadeConfiguration(ForFiles(files), objects)
        .also { configure(it) }
    )
  }
}
