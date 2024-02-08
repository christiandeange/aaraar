The plugin only needs to be applied to modules you intend to publish as artifacts.

=== "Kotlin"

    ```kotlin
    // build.gradle.kts

    plugins {
      id("sh.christian.aaraar") version "0.0.12"
    }
    ```

=== "Groovy"

    ```groovy
    // build.gradle

    plugins {
      id("sh.christian.aaraar") version "0.0.12"
    }
    ```

For Android modules, aaraar is enabled to run automatically as part of the assemble pipeline for all variants unless you
configure it otherwise via the provided `aaraar` extension. It is recommended that you only enable aaraar for variant(s)
you intend to publish.

```kotlin
aaraar {
  isEnabledForVariant { variant ->
    variant.name == "release"
  }
}
```
