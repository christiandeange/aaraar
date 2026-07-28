package sh.christian.aaraar.merger.impl

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.RTxt
import kotlin.test.Test

class RTxtMergerTest {

  private val merger = RTxtMerger()

  @Test
  fun `merges same-named symbols of different types cleanly`() {
    val libraryRTxt = RTxt(
      lines = "int string margin 0x7f010001",
      packageName = "sh.christian.library",
    )

    val dependencyRTxt = RTxt(
      lines = "int dimen margin 0x7f020001",
      packageName = "sh.christian.dependency",
    )

    val merged = merger.merge(libraryRTxt, dependencyRTxt)

    merged.toString() shouldBe """
      int dimen margin 0x0
      int string margin 0x0

    """.trimIndent()
  }

  @Test
  fun `deduplicates same-named symbols of same type`() {
    val libraryRTxt = RTxt(
      lines = "int string margin 0x7f010001",
      packageName = "sh.christian.library",
    )

    val dependencyRTxt = RTxt(
      lines = "int string margin 0x7f020001",
      packageName = "sh.christian.dependency",
    )

    val merged = merger.merge(libraryRTxt, dependencyRTxt)

    merged.toString().trim() shouldBe """
      int string margin 0x0
    """.trimIndent()
  }
}
