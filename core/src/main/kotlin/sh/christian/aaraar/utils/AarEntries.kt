package sh.christian.aaraar.utils

import java.nio.file.FileSystem
import java.nio.file.Path

// https://developer.android.com/studio/projects/android-library.html#aar-contents

val FileSystem.android_manifest: Path get() = this / "AndroidManifest.xml"
val FileSystem.classes_jar: Path get() = this / "classes.jar"
val FileSystem.res: Path get() = this / "res"
val FileSystem.r_txt: Path get() = this / "R.txt"
val FileSystem.public_txt: Path get() = this / "public.txt"
val FileSystem.assets: Path get() = this / "assets"
val FileSystem.libs: Path get() = this / "libs"
val FileSystem.jni: Path get() = this / "jni"
val FileSystem.proguard_txt: Path get() = this / "proguard.txt"
val FileSystem.lint_jar: Path get() = this / "lint.jar"
