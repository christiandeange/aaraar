package sh.christian.aaraar

import org.gradle.api.Plugin
import org.gradle.api.Project

class AarAarPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.logger.info("Hello, world!")
  }
}
