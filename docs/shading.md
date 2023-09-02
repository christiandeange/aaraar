When you embed dependencies directly into your published output, you are attributing these classes to _your_ published
artifact, not the one they originally came from. As a result, if a project consumes your published output in addition to
any external dependencies you are embedding, it's likely that compilation may fail due to duplicate classes!

This can be mitigated by **shading** classes you are embedding. Shading allows you to rename package and classes,
transforming the bytecode during the packaging step. By default, no class shading takes place, but you have the option
to configure renaming or deleting classes via the `aaraar` extension:

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

Exclusions for Android modules configured via
[`packagingOptions`](https://developer.android.com/reference/tools/gradle-api/com/android/build/api/dsl/PackagingOptions)
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
