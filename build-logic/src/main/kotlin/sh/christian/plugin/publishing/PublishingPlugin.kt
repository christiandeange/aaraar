package sh.christian.plugin.publishing

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage")
class PublishingPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(MavenPublishPlugin::class)

    val extension = target.extensions.create<PublishingExtension>("aaraar-publish")

    target.extensions.configure<MavenPublishBaseExtension> {
      pom {
        name.set("AarAar")
        description.set("A plugin for creating a merged aar file.")
        inceptionYear.set("2023")
        url.set("https://github.com/christiandeange/aaraar")

        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("https://raw.githubusercontent.com/christiandeange/aaraar/main/LICENSE")
            distribution.set("repo")
          }
        }

        developers {
          developer {
            id.set("christiandeange")
            name.set("Christian De Angelis")
            url.set("https://github.com/christiandeange")
          }
        }

        scm {
          url.set("https://github.com/christiandeange/aaraar")
          connection.set("scm:git:git://github.com/christiandeange/aaraar.git")
          developerConnection.set("scm:git:ssh://git@github.com/christiandeange/aaraar.git")
        }
      }

      publishToMavenCentral(SonatypeHost.S01)
      signAllPublications()
    }

    target.afterEvaluate {
      target.extensions.configure<MavenPublishBaseExtension> {
        coordinates(
          groupId = extension.group.get(),
          artifactId = extension.artifact.get(),
          version = extension.version.get(),
        )
      }
    }
  }
}
