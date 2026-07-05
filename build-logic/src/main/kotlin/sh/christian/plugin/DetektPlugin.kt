package sh.christian.plugin

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.plugin.DetektPlugin
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

    dependencies.add("detektPlugins", libs.detekt.rules.formatting)

    tasks.withType<Detekt>().configureEach {
      buildUponDefaultConfig.set(true)
      autoCorrect.set(false)
      config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    }
  }
}
