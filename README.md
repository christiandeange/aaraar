# aaraar

A plugin for creating a merged "fat" aar file.

Work in progress.

### Usage

<details open>
<summary>Groovy</summary>

```groovy
apply plugin: 'sh.christian.aaraar'

dependencies {
  embed project(':lib-a')
}

aaraar {
  rename 'com.example.dep.**', 'shaded.com.example.internal.@1'
  delete 'com.example.debug.**'
}
```
</details>

---

<details>
<summary>Kotlin DSL</summary>

```kotlin
apply plugin: "sh.christian.aaraar"

dependencies {
  embed(project(":lib-a"))
}

aaraar {
  rename("com.example.dep.**", "shaded.com.example.internal.@1")
  delete("com.example.debug.**")
}
```
</details>

---
