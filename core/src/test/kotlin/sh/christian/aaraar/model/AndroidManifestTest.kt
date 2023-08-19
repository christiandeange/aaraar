package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
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
}
