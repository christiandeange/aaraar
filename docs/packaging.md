Use the `embed` configuration to include dependencies in a module's packaged output.

=== "Kotlin"

    ```kotlin
    dependencies {
      compileOnly(project(":internal"))
      embed(project(":internal"))
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
      compileOnly project(":internal")
      embed project(":internal")
    }
    ```

Declaring an `embed` dependency only includes it in the packaged output, it does **not** make that dependency available
during compilation! If you wish to reference any classes in the embedded dependency in your Gradle module, you will also
need to declare it as a `compileOnly` dependency as shown above.

> **Note**
>
> `compileOnly` and `embed` dependencies will not show up in the published pom file unless also declared as `api` or
`implementation` dependencies.

In Android modules, embed configurations can also be declared for individual build types, including custom ones:

=== "Kotlin"

    ```kotlin
    android {
      buildTypes {
        create("publish") {
          initWith(buildTypes.getByName("release"))
          matchingFallbacks += "release"
        }
      }
    }

    dependencies {
      compileOnly(project(":internal"))
      publishEmbed(project(":internal"))
    }
    ```

=== "Groovy"

    ```groovy
    android {
      buildTypes {
        publish {
          initWith release
          matchingFallbacks += "release"
        }
      }
    }

    dependencies {
      compileOnly project(":internal")
      publishEmbed project(":internal")
    }
    ```
