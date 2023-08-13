### Usage

The plugin only needs to be applied to modules you intend to publish as artifacts.

=== "Kotlin"

    ```kotlin
    // build.gradle.kts

    plugins {
      id("sh.christian.aaraar") version "0.0.9"
    }
    ```

=== "Groovy"

    ```groovy
    // build.gradle

    plugins {
      id("sh.christian.aaraar") version "0.0.9"
    }
    ```

By default, aaraar is enabled to run automatically as part of the assemble pipeline for all variants, unless you
configure it otherwise via the provided `aaraar` extension. It is recommended that you only enable aaraar for variant(s)
you intend to publish.

```kotlin
aaraar {
  isEnabledForVariant { variant ->
    variant.name == "release"
  }
}
```

### Packaging

Use the `embed` configuration to include dependencies in a module's packaged aar.

=== "Kotlin"

    ```kotlin
    dependencies {
      implementation(project(":internal"))
      embed(project(":internal"))
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
      implementation project(":internal")
      embed project(":internal")
    }
    ```

Embed configurations can also be declared for individual build types, even custom ones:

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
      implementation(project(":internal"))
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
      implementation project(":internal")
      publishEmbed project(":internal")
    }
    ```

The `embed` configuration does not affect which dependencies are available at compilation time, so you will still need
to declare dependencies as `implementation`, `api`, etc. as per usual to compile against them.

Dependencies marked as `api` will be included as `compile` dependencies in the published pom file.
`implementation` dependencies will be included as `runtime` in the pom.

> **Note**
>
> `compileOnly` and `embed` dependencies will not show up in the published pom file unless also marked as `api` or
`implementation` dependencies.

### Shading

Since embedded dependencies can often lead to duplicate class declarations for consumers, you also have the ability to
shade classes being packaged into the final aar. By default, all classes will remain unchanged, but you have the option
to rename or delete classes as well:

=== "Kotlin"

    ```kotlin
    aaraar {
      rename("io.reactivex.**", "shaded.io.reactivex.@1")

      delete("com.myapp.debug.**")
      delete("com.myapp.superdupersecret.PrivateKeyProvider")
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      rename "io.reactivex.**", "shaded.io.reactivex.@1"

      delete "com.myapp.debug.**"
      delete "com.myapp.superdupersecret.PrivateKeyProvider"
    }
    ```

Classes can be specified by matching against a pattern that supports two wildcard types:

- `*` will match a single package component.
- `**` will match against the remainder of any valid fully-qualified class name.

For class renames, the replacement string is a class name which can reference the substrings matched by the wildcards.
A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
A special `@0` reference contains the entire matched class name.

Exclusions configured via [`packagingOptions`](https://developer.android.com/reference/tools/gradle-api/com/android/build/api/dsl/PackagingOptions)
to delete resource files will also be respected:

```kotlin
android {
  packagingOptions {
    resources {
      excludes += "**/module-info.class"
    }
  }
}
```

### Publishing

The merged aar is included in a Gradle `SoftwareComponent` that you can publish using your plugin of choice.
One component is created per library variant, using the same name as the variant.

Below are examples of how to integrate this with the two most common publishing plugins:

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
