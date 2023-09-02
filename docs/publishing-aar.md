The merged aar is included in a Gradle `SoftwareComponent` that you can publish using your plugin of choice.
One component is created per library variant, using the same name as the variant.

Integrating publishing with common publishing plugins is very simple, but direct access to the generated `aar` file
is also available if a custom publishing solution is needed.

???+ note "maven-publish"

    [https://docs.gradle.org/current/userguide/publishing_maven.html](https://docs.gradle.org/current/userguide/publishing_maven.html)

    === "Kotlin"

        ```kotlin
        afterEvaluate {
          publishing {
            publications {
              create<MavenPublication>("maven") {
                from(components["release"])
              }
            }
          }
        }
        ```

    === "Groovy"

        ```groovy
        afterEvaluate {
          publishing {
            publications {
              maven(MavenPublication) {
                from(components.release)
              }
            }
          }
        }
        ```

??? note "com.vanniktech.maven.publish"

    [https://github.com/vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)

    You will need to specify the variant name of the merged aar you want to publish via a project property:

    ```kotlin
    project.ext.set("ANDROID_VARIANT_TO_PUBLISH", "release")
    ```

??? note "Custom Publishing"

    If you have your own custom publishing step, you can reference the generated `aar` file as a property like so:
    
    === "Kotlin"
    
        ```kotlin
        abstract class MyCustomPublishTask {
          @get:InputFile
          abstract val inputJar: RegularFileProperty
    
          // ...
        }
    
        tasks.named<MyCustomPublishTask>("publish") {
          inputAar.set(tasks.named<PackageAarTask>("packageReleaseAar").flatMap { it.outputAar })
        }
        ```
    
    === "Groovy"
    
        ```groovy
        abstract class MyCustomPublishTask {
          @InputFile
          abstract RegularFileProperty inputJar;
    
          // ...
        }
    
        tasks.named("publish", MyCustomPublishTask).configureEach {
          inputAar.set(tasks.named("packageReleaseAar", PackageAarTask).flatMap { it.outputAar })
        }
        ```

If using Android Gradle Plugin 8.0 or higher, make sure you've also set up variant publishing. Consult with the
[variant publication documentation](https://developer.android.com/build/publish-library/configure-pub-variants) if you
need additional customization.

=== "Kotlin"

    ```kotlin
    android {
      publishing {
        singleVariant("release")
      }
    }
    ```

=== "Groovy"

    ```groovy
    android {
      publishing {
        singleVariant "release"
      }
    }
    ```

