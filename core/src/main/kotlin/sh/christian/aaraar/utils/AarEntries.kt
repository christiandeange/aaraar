package sh.christian.aaraar.utils

import java.nio.file.FileSystem
import java.nio.file.Path

// https://developer.android.com/studio/projects/android-library.html#aar-contents

/**
 * The full path to the `aar-metadata.properties` file within an AAR file.
 */
val FileSystem.aarMetadataProperties: Path
  get() = this / "META-INF" / "com" / "android" / "build" / "gradle" / "aar-metadata.properties"

/**
 * The full path to the `AndroidManifest.xml` file within an AAR file.
 */
val FileSystem.androidManifestXml: Path get() = this / "AndroidManifest.xml"

/**
 * The full path to the `classes.jar` file within an AAR file.
 */
val FileSystem.clasesJar: Path get() = this / "classes.jar"

/**
 * The full path to the `res` file within an AAR file.
 */
val FileSystem.res: Path get() = this / "res"

/**
 * The full path to the `R.txt` file within an AAR file.
 */
val FileSystem.rTxt: Path get() = this / "R.txt"

/**
 * The full path to the `public.txt` file within an AAR file.
 */
val FileSystem.publicTxt: Path get() = this / "public.txt"

/**
 * The full path to the `assets` file within an AAR file.
 */
val FileSystem.assets: Path get() = this / "assets"

/**
 * The full path to the `libs` file within an AAR file.
 */
val FileSystem.libs: Path get() = this / "libs"

/**
 * The full path to the `jni` file within an AAR file.
 */
val FileSystem.jni: Path get() = this / "jni"

/**
 * The full path to the `proguard.txt` file within an AAR file.
 */
val FileSystem.proguardTxt: Path get() = this / "proguard.txt"

/**
 * The full path to the `lint.jar` file within an AAR file.
 */
val FileSystem.lintJar: Path get() = this / "lint.jar"

/**
 * The full path to the `navigation.json` file within an AAR file.
 */
val FileSystem.navigationJson: Path get() = this / "navigation.json"
