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

!!! note "Note"

    `compileOnly` and `embed` dependencies will not show up in the published pom file unless also declared as `api` or
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

!!! bug "Known Issue"

    At this time, creating new enum classes or modifications to existing enum classes will be ignored.

An example implementation can be seen below, which is used to remove a public class and a public method that are both
meant for internal use only:

=== "Kotlin"

    ```kotlin
    class MyApiJarProcessor : ApiJarProcessor() {
      override fun processClasspath(aarArchive: AarArchive, classpath: Classpath) {
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
    class MyApiJarProcessor extends ApiJarProcessor {
      @Override
      void processClasspath(@NotNull AarArchive aarArchive, @NotNull Classpath classpath) {
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
