package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.ktLibraryJarPath
import kotlin.test.Test

class AssetsTest {
  @Test
  fun `test equality`() {
    val assets1 = Assets.from(ktLibraryJarPath.parent)
    val assets2 = Assets.from(ktLibraryJarPath.parent)
    assets1 shouldBe assets2
  }
}
