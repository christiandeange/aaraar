package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AndroidManifestTest {

  @Test
  fun `parses package name from manifest`() {
    val manifest = AndroidManifest("""<manifest package="com.library.main" />""")
    manifest.packageName shouldBe "com.library.main"
  }

  @Test
  fun `parses minSdkVersion from manifest`() {
    val manifest = AndroidManifest(
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
  fun `test toString`() {
    val manifestString =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
      	<uses-sdk android:minSdkVersion="21" android:targetSdkVersion="30"/>
      </manifest>
      """.trimIndent()

    val manifest = AndroidManifest(manifestString)
    manifest.toString() shouldBe manifestString
  }

  @Test
  fun `test equality`() {
    val manifestString =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
      	<uses-sdk android:minSdkVersion="21" android:targetSdkVersion="30"/>
      </manifest>
      """.trimIndent()

    val manifest1 = AndroidManifest(manifestString)
    val manifest2 = AndroidManifest(manifestString)
    manifest1 shouldBe manifest2
  }
}
