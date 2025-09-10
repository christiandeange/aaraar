package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ProguardTest {
  private val contents = """
    -keep class androidx.** { *; }
    -keep class com.google.** {
      <fields>;
      <methods>;
    }

    -dontwarn com.android.**
  """.trimIndent()

  @Test
  fun `test toString`() {
    val proguard1 = Proguard(contents.lines())
    proguard1.toString() shouldBe contents
  }

  @Test
  fun `test equality`() {
    val proguard1 = Proguard(contents.lines())
    val proguard2 = Proguard(contents.lines())
    proguard1 shouldBe proguard2
  }
}
