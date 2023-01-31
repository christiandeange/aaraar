# aaraar

A plugin for creating a merged aar file.

Work in progress.

### Usage

```kotlin
// build.gradle[.kts]

plugins {
  id("sh.christian.aaraar") version "0.0.2"
}

dependencies {
  embed(project(":lib-a"))
}

aaraar {
  rename("com.external.**", "shaded.com.external.@1")
  delete("com.internal.debug.**")
}
```
