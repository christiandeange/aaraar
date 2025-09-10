package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.ktLibraryJarPath
import kotlin.test.Test

class ResourcesTest {
  @Test
  fun `test equality`() {
    val resources1 = Resources(
      files = FileSet.fromFileTree(ktLibraryJarPath.parent)!!,
      packageName = "sh.christian.example",
      minSdk = 21,
      androidAaptIgnore = "",
    )
    val resources2 = Resources(
      files = FileSet.fromFileTree(ktLibraryJarPath.parent)!!,
      packageName = "sh.christian.example",
      minSdk = 21,
      androidAaptIgnore = "",
    )
    resources1 shouldBe resources2
  }
}
