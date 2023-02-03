package sh.christian.plugin

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

@Suppress("UnstableApiUsage")
class PublishingPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val groupId = target.stringProperty("POM_GROUP_ID")
    val artifactId = target.path.trimStart(':').replace(':', '-')
    val version = target.stringProperty("POM_VERSION")

    target.group = groupId
    target.version = version

    target.plugins.apply(MavenPublishPlugin::class)

    target.extensions.configure<MavenPublishBaseExtension> {
      coordinates(
        groupId = groupId,
        artifactId = artifactId,
        version = version,
      )

      pom {
        name.set(target.stringProperty("POM_NAME"))
        description.set(target.stringProperty("POM_DESCRIPTION"))
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

      publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
      signAllPublications()
    }
  }

  private fun Project.stringProperty(name: String): String {
    return property(name) as String
  }
}
