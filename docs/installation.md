The plugin only needs to be applied to modules you intend to publish as artifacts.

=== "Kotlin"

    ```kotlin
    // build.gradle.kts

    plugins {
      id("sh.christian.aaraar") version "0.0.18"
    }
    ```

=== "Groovy"

    ```groovy
    // build.gradle

    plugins {
      id("sh.christian.aaraar") version "0.0.18"
    }
    ```

### Android modules

For Android modules, aaraar is configured to run automatically as part of the assemble pipeline for all variants, unless
configured otherwise via the provided `aaraar` extension. It is recommended that you only enable aaraar for variant(s)
you intend to publish.

```kotlin
aaraar {
  isEnabledForVariant { variant ->
    variant.name == "release"
  }
}
```

### JVM modules

By default, the `packageJar` task will overwrite the output of the `jar` task with the merged jar file, but this can
be customized to suit your needs by changing the `PackageJar.outputJar` task output file property.

=== "Kotlin"

    ```kotlin
    tasks.named<PackageJarTask>("packageJar") {
      isEnabled = providers.gradleProperty("enablePublishing").map { it.toBoolean() }.getOrElse(false)

      outputJar.set(project.layout.buildDirectory.file("artifact-all.jar"))
    }

    // Run via ./gradlew -PenablePublishing=true [task_name]
    ```

=== "Groovy"

    ```groovy
    tasks.named("packageJar", PackageJarTask) {
      setEnabled(providers.gradleProperty("enablePublishing").map { it.toBoolean() }.getOrElse(false))

      outputJar = project.layout.buildDirectory.file("artifact-all.jar")
    }

    // Run via ./gradlew -PenablePublishing=true [task_name]
    ```
