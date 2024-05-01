The merged jar is included in a Gradle `SoftwareComponent` that you can publish using your plugin of choice.

Integrating publishing with common publishing plugins is very simple, but direct access to the generated `jar` file
is also available if a custom publishing solution is needed.

???+ note "maven-publish"

    [https://docs.gradle.org/current/userguide/publishing_maven.html](https://docs.gradle.org/current/userguide/publishing_maven.html)

    === "Kotlin"

        ```kotlin
        afterEvaluate {
          publishing {
            publications {
              create<MavenPublication>("maven") {
                from(components["java"])
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
                from(components.java)
              }
            }
          }
        }
        ```

??? note "com.vanniktech.maven.publish"

    [https://github.com/vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)

    No configuration needed! Works right out of the box.

??? note "Custom Publishing"

    If you have your own custom publishing step, you can reference the generated `jar` file as a property like so:
    
    === "Kotlin"
    
        ```kotlin
        abstract class MyCustomPublishTask {
          @get:InputFile
          abstract val inputJar: RegularFileProperty
    
          // ...
        }
    
        tasks.named<MyCustomPublishTask>("publish") {
          inputJar.set(tasks.named<PackageJarTask>("packageJar").flatMap { it.outputJar })
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
          inputJar.set(tasks.named("packageJar", PackageJarTask).flatMap { it.outputJar })
        }
        ```
