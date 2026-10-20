When you embed dependencies directly into your published output, you are attributing these classes to _your_ published
artifact, not the one they originally came from. As a result, if a project consumes your published output in addition to
any external dependencies you are embedding, it's likely that compilation may fail due to duplicate classes!

This can be mitigated by **shading** classes you are embedding. Shading allows you to rename package and classes,
transforming the bytecode during the packaging step. Shading rules can be specified via the `aaraar` extension, and can
be applied universally or scoped to a particular set of projects or dependencies.

## Rules

Classes can be specified by matching against a pattern that supports two wildcard types:

- `*` will match a single package component.
- `**` will match against the remainder of any valid fully-qualified class name.

For class renames, the replacement string is a class name which can reference the substrings matched by the wildcards.
A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
A special `@0` reference contains the entire matched class name.

Exclusions for Android modules configured via
[`packaging`](https://developer.android.com/reference/tools/gradle-api/com/android/build/api/dsl/Packaging)
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

## Scopes

There are multiple ways to specify a scope for shading rules:

- **All**: Applies to all classes and resource files in the merged file.
- **Project**: Applies to a single project (eg: `project(":internal")`)
- **Group**: Applies to any artifact within a dependency group (eg: [`io.reactivex.rxjava3`](https://mvnrepository.com/artifact/io.reactivex.rxjava3))
- **Module**: Applies to any version of a dependency (eg: [`io.reactivex.rxjava3:rxjava`](https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava))
- **Dependency**: Applies to a single version of a dependency (eg: [`io.reactivex.rxjava3:rxjava:3.1.8`](https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava/3.1.8))
- **Files**: Applies to a file, set of files, or an entire file tree (eg: `fileTree("libs")`)

Rules can be applied to one or more matching scope.

When classes are renamed or deleted, aaraar also updates Java `ServiceLoader` provider configuration files and Kotlin
`.kotlin_module` metadata files. If a deletion removes every class represented by a Kotlin module metadata file, that
metadata file is removed as well.

## Examples

Shading an embedded external dependency to prevent conflicts:

=== "Kotlin"

    ```kotlin
    aaraar {
      shading {
        createRule(forProject(path) and forGroup("io.reactivex.rxjava3")) {
          rename("io.reactivex.**", "shaded.io.reactivex.@1")
        }
      }
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      shading {
        createRule([forProject(path), forGroup("io.reactivex.rxjava3")]) {
          it.rename "io.reactivex.**", "shaded.io.reactivex.@1"
        }
      }
    }
    ```

Removing all internal debug classes from production code:

=== "Kotlin"

    ```kotlin
    aaraar {
      shading {
        createRule {
          delete("com.myapp.debug.**")
          delete("com.myapp.LocalEnvironmentKeyProvider")
        }
      }
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      shading {
        createRule {
          it.delete "com.myapp.debug.**"
          it.delete "com.myapp.LocalEnvironmentKeyProvider"
        }
      }
    }
    ```

Removing an unused feature from an external dependency:

=== "Kotlin"

    ```kotlin
    aaraar {
      shading {
        createRule(forProject(path) and forDependency(libs.bouncycastle.prov)) {
          delete("org.bouncycastle.x509.**")
        }
      }
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      shading {
        createRule([forProject(path), forDependency(libs.bouncycastle.prov)]) {
          it.delete "org.bouncycastle.x509.**"
        }
      }
    }
    ```

Renaming classes from static jar files:

=== "Kotlin"

    ```kotlin
    aaraar {
      shading {
        createRule(forProject(path) and forFiles(fileTree("libs/debug/") { include("*.jar") })) {
          rename("com.**", "com.debug.@1")
        }
      }
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      shading {
        createRule([forProject(path), forFiles(fileTree(dir: "libs/debug/", include: ["*.jar"]))]) {
          it.rename "com.**", "com.debug.@1"
        }
      }
    }
    ```

The `rename()` and `delete()` rules apply to both class files and resource files. If a rule should affect only a
resource path, use `renameResource()` or `deleteResource()` instead. Resource paths use `/` separators and support the
same `*`, `**`, and `@0`/`@1` replacement syntax as class rules.

=== "Kotlin"

    ```kotlin
    aaraar {
      shading {
        createRule(forProject(path) and forGroup("com.example")) {
          renameResource("META-INF/NOTICE", "META-INF/licenses/example-NOTICE")
          deleteResource("META-INF/*.SF")
        }
      }
    }
    ```

=== "Groovy"

    ```groovy
    aaraar {
      shading {
        createRule([forProject(path), forGroup("com.example")]) {
          it.renameResource "META-INF/NOTICE", "META-INF/licenses/example-NOTICE"
          it.deleteResource "META-INF/*.SF"
        }
      }
    }
    ```
