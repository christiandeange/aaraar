package sh.christian.aaraar

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.utils.withFile
import kotlin.test.Test

class AndroidManifestTest {

  @Test
  fun `parses package name from manifest`() {
    val manifest = AndroidManifest.from("""<manifest package="com.library.main" />""")
    manifest.packageName shouldBe "com.library.main"
  }

  @Test
  fun `parses minSdkVersion from manifest`() {
    val manifest = AndroidManifest.from(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <uses-sdk
              android:minSdkVersion="21"
              android:targetSdkVersion="30"/>
      </manifest>
      """
    )

    manifest.minSdk shouldBe 21
  }

  @Test
  fun `keeps package name from main manifest file`() {
    val mainLibManifest = AndroidManifest.from("""<manifest package="com.library.main" />""")
    val helperModuleManifest = AndroidManifest.from("""<manifest package="com.library.helper.core" />""")

    val mergedManifest = mainLibManifest + helperModuleManifest

    mergedManifest shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """
  }

  @Test
  fun `adds new nodes from dependencies`() {
    val mainLibManifest = AndroidManifest.from("""<manifest package="com.library.main" />""")

    val helperModuleManifest = AndroidManifest.from(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
      </manifest>
      """
    )

    val mergedManifest = mainLibManifest + helperModuleManifest

    mergedManifest shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
          <application/>
      </manifest>
      """
  }

  @Test
  fun `applies tool rules to manifests from dependencies`() {
    val mainLibManifest = AndroidManifest.from(
      """
      <manifest
              xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:tools="http://schemas.android.com/tools"
              package="com.library.main">

          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" tools:node="remove" />
      </manifest>
      """
    )

    val helperModuleManifest = AndroidManifest.from(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
      </manifest>
      """
    )

    val mergedManifest = mainLibManifest + helperModuleManifest

    mergedManifest shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """
  }

  @Test
  fun `keeps placeholders`() {
    val mainLibManifest = AndroidManifest.from("""<manifest package="com.library.main" />""")

    val helperModuleManifest = AndroidManifest.from(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <application>
              <activity
                  android:name=".MainActivity"
                  android:screenOrientation="${'$'}screenOrientation}" />
          </application>
      </manifest>
      """
    )

    val mergedManifest = mainLibManifest + helperModuleManifest

    mergedManifest shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application>
              <activity android:name="com.library.helper.core.MainActivity" android:screenOrientation="${'$'}screenOrientation}"/>
          </application>
      </manifest>
      """
  }

  private infix fun AndroidManifest.shouldBe(contents: String) {
    withFile {
      writeTo(filePath)
      string().trim().replace("\t", "    ") shouldBe contents.trimIndent()
    }
  }
}
