package sh.christian.aaraar.utils

import java.nio.file.FileSystem
import java.nio.file.Path

// https://developer.android.com/studio/projects/android-library.html#aar-contents

internal val FileSystem.aar_metadata: Path
  get() = this / "META-INF" / "com" / "android" / "build" / "gradle" / "aar-metadata.properties"

internal val FileSystem.android_manifest: Path get() = this / "AndroidManifest.xml"
internal val FileSystem.classes_jar: Path get() = this / "classes.jar"
internal val FileSystem.res: Path get() = this / "res"
internal val FileSystem.r_txt: Path get() = this / "R.txt"
internal val FileSystem.public_txt: Path get() = this / "public.txt"
internal val FileSystem.assets: Path get() = this / "assets"
internal val FileSystem.libs: Path get() = this / "libs"
internal val FileSystem.jni: Path get() = this / "jni"
internal val FileSystem.proguard_txt: Path get() = this / "proguard.txt"
internal val FileSystem.lint_jar: Path get() = this / "lint.jar"
internal val FileSystem.navigation_json: Path get() = this / "navigation.json"
