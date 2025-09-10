package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AarMetadataTest {
  private val contents = """
    aarFormatVersion=1.0
    aarMetadataVersion=1.0
    minCompileSdk=1
    minCompileSdkExtension=0
    minAndroidGradlePluginVersion=1.0.0
    coreLibraryDesugaringEnabled=false
  """.trimIndent()

  @Test
  fun `test toString`() {
    val metadata = AarMetadata(contents.lines())
    metadata.toString() shouldBe contents
  }

  @Test
  fun `test equality`() {
    val metadata1 = AarMetadata(contents.lines())
    val metadata2 = AarMetadata(contents.lines())
    metadata1 shouldBe metadata2
  }
}
