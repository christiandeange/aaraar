The aaraar Gradle plugin can be applied to any module that is published as an `aar` or a `jar` file. For Android
modules, the following entries in an `aar` file are recognized for merging:

- Class files
- Android manifests
- Android resources
- Android assets
- Local `jar` libraries
- Native JNI libraries
- Proguard consumer rules
- Lint consumer checks
- Navigation graphs
- `api.jar` files
- `aar` metadata

### Gradle Configuration

The Gradle plugin exposes two configurations for you to include dependencies in a module's packaged output:

| Configuration | Description                                                                             |
|---------------|-----------------------------------------------------------------------------------------|
| `embed`       | Include only this dependency in the packaged output.                                    |
| `embedTree`   | Include this dependency and all of its own runtime dependencies in the packaged output. |

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

Declaring an embedded dependency only includes it in the packaged output, it does **not** make that dependency available
during compilation! If you wish to reference any classes in the embedded dependency in your Gradle module, you will also
need to declare it as a `compileOnly` dependency as shown above. You can also configure this automatically by making
embedded dependencies available during compilation:

=== "Kotlin"

    ```kotlin
    dependencies {
      embed(project(":internal"))
    }

    configurations.compileOnly.configure {
      extendsFrom(configurations["embed"])
      extendsFrom(configurations["embedTree"])
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
      embed project(":internal")
    }

    configurations.compileOnly {
      extendsFrom configurations.embed
      extendsFrom configurations.embedTree
    }
    ```

Adding embedded dependencies to `compileOnly` is intentional; if they are also added as `implementation` or `api`
dependencies, they will show up in the published POM file, causing duplicated class conflicts for downstream consumers
of your merged artifact!

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

`embedTree` is necessary if you wish to embed a dependency that has its own transitive runtime dependencies, especially
for third-party dependencies. If such a dependency was included in `embed` instead, projects that consume the merged
artifact will be responsible for providing their own dependencies at runtime for the missing dependencies.
The difference between `embed` and `embedTree` can be seen when running a Gradle task to visualize the dependency tree
for a given configuration:

=== "embed"

    === "Kotlin"

        ```kotlin
        dependencies {
          releaseEmbed("com.google.crypto.tink:tink-android:1.16.0")
        }
        ```

    === "Groovy"

        ```groovy
        dependencies {
          releaseEmbed 'com.google.crypto.tink:tink-android:1.16.0'
        }
        ```

    ```
    $ ./gradlew :app:dependencies --configuration releaseEmbedClasspath

    releaseEmbedClasspath
    \--- com.google.crypto.tink:tink-android:1.16.0
    ```

=== "embedTree"

    === "Kotlin"

        ```kotlin
        dependencies {
          releaseEmbedTree("com.google.crypto.tink:tink-android:1.16.0")
        }
        ```

    === "Groovy"

        ```groovy
        dependencies {
          releaseEmbedTree 'com.google.crypto.tink:tink-android:1.16.0'
        }
        ```

    ```
    $ ./gradlew :app:dependencies --configuration releaseEmbedClasspath

    releaseEmbedClasspath
    \--- com.google.crypto.tink:tink-android:1.16.0
     +--- androidx.annotation:annotation-jvm:1.8.2
     |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.7.10
     |         +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.7.10
     |         \--- org.jetbrains:annotations:13.0
     +--- com.google.code.findbugs:jsr305:3.0.2
     +--- com.google.code.gson:gson:2.10.1
     \--- com.google.errorprone:error_prone_annotations:2.22.0
    ```

### META-INF files

You can also let the plugin know whether to strip all `META-INF/` files from the packaged output file. By default it
keeps them all, but this can be configured via the plugin extension:

=== "Kotlin"

    ```kotlin
    aaraar {
      // Strip all META-INF/ files from the merged file
      keepMetaFiles.set(false)
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      // Strip all META-INF/ files from the merged file
      keepMetaFiles = false
    }
    ```

### Packaging Options for Android Libraries

The [`packaging`](https://developer.android.com/reference/tools/gradle-api/com/android/build/api/dsl/Packaging) block in
an Android module's build script is used to handle the packaging of resources and JNI libraries. This plugin also uses
those same rules to configure handling of resources and JNI libraries merge conflicts when merging multiple aar files
together.

### Post-Processing

The plugin allows you to register custom processors that will be executed after the merged archive has been created.
This can be useful for logging, validation, or applying other operations that you may want to perform on the merged
archive. To register a processor, you must first create a factory that will be used to create instances of your custom
processor via the plugin extension:

=== "Kotlin"

    ```kotlin
    aaraar {
      addPostProcessorFactory(ClassLoggingProcessorFactory())
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      addPostProcessorFactory(new ClassLoggingProcessorFactory())
    }
    ```

Note that your factory must be `Serializable`.

The factory can also be registered by passing in only the class name, which will be used to reflective instantiate an
instance at task execution. This is useful if your factory is not available on the Gradle buildscript classpath.
In doing so, you must ensure that your factory has a public no-arg constructor so that it can be properly created:

=== "Kotlin"

    ```kotlin
    aaraar {
      addPostProcessorFactory("com.example.gradle.ClassLoggingProcessorFactory")
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      addPostProcessorFactory("com.example.gradle.ClassLoggingProcessorFactory")
    }
    ```

Processors are executed in the order they are registered. The factory interface is very straightforward and only defines
a `create()` method for you to implement. This method should return an instance of your custom
`ArtifactArchiveProcessor` implementation that will be executed on the merged archive.

=== "Kotlin"

    ```kotlin
    class ClassLoggingProcessor : ArtifactArchiveProcessor {
        override fun process(archive: ArtifactArchive): ArtifactArchive {
            println("Merged archive contains: ${archive.classes.archive.count()} classes.")
            return archive
        }
    }
    ```

=== "Groovy"

    ```groovy
    class ClassLoggingProcessor implements ArtifactArchiveProcessor {
        @Override
        ArtifactArchive process(@NotNull ArtifactArchive archive) {
            println "Merged archive contains: ${archive.classes.archive.size()} classes."
            return archive
        }
    }
    ```

### API Jar for Android Libraries

The `api.jar` file is an optional element inside an aar archive that helps developers using the library understand its
exposed classes, methods, and functionalities. When this file exists in an aar, it will be used it as the source of
truth for which members can be referenced at compilation time.

Generating a custom `api.jar` file can be used to hide certain public members from IDE autocomplete, though they
can still be referenced and invoked via reflection at runtime as per usual.

Generating this file begins with registering an `ArtifactArchiveProcessor.Factory` factory to create a subclass of
`ApiJarProcessor`. The processor implementation is where the `api.jar` transformation occurs. When enabled, it is
provided with a representation of the current set of classes defined in the merged aar archive, as well as a reference
to the merged archive itself. While the archive is immutable, the classpath provided is mutable and supports a variety
of transformations, including but not limited to:

- Removing existing classes and class members (constructors, methods, and fields).
- Defining entirely new classes with custom members.
- Renaming classes and class members or modifying their access visibility.
- Adding or removing annotations on classes and class members.

An example implementation can be seen below, which is used to remove a public class and a public method that are both
meant for internal use only:

=== "Kotlin"

    ```kotlin
    class MyApiJarProcessor : ApiJarProcessor {
      override fun processClasspath(aarArchive: AarArchive, classpath: MutableClasspath) {
        // Remove internal class
        classpath.removeClass("com.example.TerminalSdkInternal")

        // Remove internal method on public class
        val fooClass = classpath.get("com.example.TerminalSdk")
        fooClass.methods = fooClass.methods.filter { !it.name.contains("internal", ignoreCase = true) }
      }
    }
    ```

=== "Groovy"

    ```groovy
    class MyApiJarProcessor implements ApiJarProcessor {
      @Override
      void processClasspath(@NotNull AarArchive aarArchive, @NotNull MutableClasspath classpath) {
        // Remove internal class
        classpath.removeClass("com.example.TerminalSdkInternal")

        // Remove internal method on public class
        def fooClass = classpath.get("com.example.TerminalSdk")
        fooClass.methods = fooClass.methods.findAll { !it.name.containsIgnoreCase("internal") }
      }
    }
    ```

Further documentation for the kind of transformations available can be found by referencing
[API documentation](https://aaraar.christian.sh/kdoc/aaraar/sh.christian.aaraar.model.classeditor/index.html) for the
`sh.christian.aaraar.model.classeditor` package.

!!! bug "Modifying Enums"

    At this time, creating new enum classes or modifications to existing enum classes will be ignored.

!!! note "Modifying Parameter Names"

    By default, parameter name metadata is not included in class files. If you wish to include parameter names in the
    compiled `classes.jar` and `api.jar` files, you must compile your library with additional flags in order to embed
    this data in your compiled class files:

    === "Kotlin"

        ```kotlin
        // build.gradle.kts

        tasks.withType<KotlinCompile>().configureEach {
          compilerOptions.freeCompilerArgs.add("-java-parameters")
        }
        tasks.withType<JavaCompile>().configureEach {
          options.compilerArgs.add("-parameters")
        }
        ```

    === "Groovy"

        ```groovy
        // build.gradle

        tasks.withType(KotlinCompile).configureEach {
          compilerOptions.freeCompilerArgs += "-java-parameters"
        }
        tasks.withType(JavaCompile).configureEach {
          options.compilerArgs += "-parameters"
        }
        ```
