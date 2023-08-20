package sh.christian.aaraar.merger.impl

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.AndroidManifest
import kotlin.test.Test

class AndroidManifestMergerTest {

  private val merger = AndroidManifestMerger()

  @Test
  fun `keeps package name from main manifest file`() {
    val mainLibManifest = AndroidManifest.from("""<manifest package="com.library.main" />""")
    val helperModuleManifest = AndroidManifest.from("""<manifest package="com.library.helper.core" />""")

    val mergedManifest = merger.merge(mainLibManifest, helperModuleManifest)

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

    val mergedManifest = merger.merge(mainLibManifest, helperModuleManifest)

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

    val mergedManifest = merger.merge(mainLibManifest, helperModuleManifest)

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

    val mergedManifest = merger.merge(mainLibManifest, helperModuleManifest)

    mergedManifest shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application>
              <activity android:name="com.library.helper.core.MainActivity" android:screenOrientation="${'$'}screenOrientation}"/>
          </application>
      </manifest>
      """
  }

  private infix fun AndroidManifest.shouldBe(contents: String) {
    toString().trim().replace("\t", "    ") shouldBe contents.trimIndent()
  }
}
