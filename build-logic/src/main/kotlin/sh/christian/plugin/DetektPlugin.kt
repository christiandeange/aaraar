package sh.christian.plugin

import io.gitlab.arturbosch.detekt.CONFIGURATION_DETEKT_PLUGINS
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class DetektPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    val libs = the<LibrariesForLibs>()

    pluginManager.apply(DetektPlugin::class)

    dependencies.add(CONFIGURATION_DETEKT_PLUGINS, libs.detekt.rules.formatting)

    tasks.withType<Detekt>().configureEach {
      buildUponDefaultConfig = true
    }
  }
}
