![Maven Central](https://img.shields.io/maven-central/v/sh.christian.aaraar/gradle-plugin?versionPrefix=0.0.8) ![CI](https://github.com/christiandeange/aaraar/actions/workflows/ci.yml/badge.svg)

# aaraar

A plugin for creating a merged aar file.

Work in progress.

### Usage

```kotlin
// build.gradle[.kts]

plugins {
  id("sh.christian.aaraar") version "0.0.8"
}
```

### Packaging

Use the `embed` configuration to include dependencies in a module's packaged aar.

```kotlin
dependencies {
  implementation(project(":internal"))
  embed(project(":internal"))
}
```

Embed configurations can also be declared for individual build types, even custom ones:

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

The `embed` configuration does not affect which dependencies are available at compilation time, so you will still need
to declare dependencies as `implementation`, `api`, etc. as per usual to compile against them.

Dependencies marked as `api` will be included as `compile` dependencies in the published pom file.
`implementation` dependencies will be included as `runtime` in the pom.

> **Note**
> 
> `compileOnly` and `embed` dependencies will not show up in the published pom file unless also marked as `api` or
`implementation` dependencies.

By default, aaraar is enabled to run automatically as part of the assemble pipeline for all variants, unless you
configure it otherwise via the provided `aaraar` extension. It is recommended that you only enable aaraar for variant(s)
you intend to publish.

```kotlin
aaraar {
  isEnabledForVariant { variant ->
    variant.name == "publish"
  }
}
```

If using Android Gradle Plugin 8.0 or higher, make sure you've also set up variant publishing. Consult with the
[variant publication documentation](https://developer.android.com/build/publish-library/configure-pub-variants) if you
need additional customization.

```kotlin
android {
  publishing {
    singleVariant("publish")
  }
}
```

### Shading

Since embedded dependencies can often lead to duplicate class declarations for consumers, you also have the ability to
shade classes being packaged into the final aar. By default, all classes will remain unchanged, but you have the option
to rename or delete classes as well:

```kotlin
aaraar {
  rename("io.reactivex.**", "shaded.io.reactivex.@1")

  delete("com.myapp.debug.**")
  delete("com.myapp.superdupersecret.PrivateKeyProvider")
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
One component is created per library variant, using the same name.

Below are examples of how to integrate this with the two most common publishing plugins:

<details>
<summary><b>maven-publish</b></summary>

<br/>

https://docs.gradle.org/current/userguide/publishing_maven.html

 *    <details open>
      <summary>Kotlin DSL</summary>
      
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
      </details>

 *    <details>
      <summary>Groovy</summary>
      
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
      </details>
</details>

<details>
<summary><b>gradle-maven-publish-plugin</b></summary>

<br/>

https://github.com/vanniktech/gradle-maven-publish-plugin

You will need to specify which flavor of the merged aar you want to publish via a project property:

```kotlin
project.ext.set("ANDROID_VARIANT_TO_PUBLISH", "release")
```

> **Note**
>
> You could also always set this property through the command line, but neglecting to do so will cause it to
default to `"release"`, which will **not** be the merged aar artifact produced by this plugin.
</details>

# License

```
Copyright 2023 Christian De Angelis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
