package sh.christian.aaraar.model

import io.kotest.assertions.withClue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.withFileSystem
import kotlin.test.Test

class FileSetTest {

  @Test
  fun `from nonexistent file tree returns null`() {
    withFileSystem {
      (FileSet.fromFileTree(root / "Documents and Settings")) shouldBe null
    }
  }

  @Test
  fun `from empty file tree returns empty FileSet`() {
    withFileSystem {
      val fileSet = FileSet.fromFileTree(root)
      withClue("FileSet from root folder") {
        fileSet shouldNotBe null
      }

      withDirectory {
        fileSet!!.writeTo(root)
        files().shouldBeEmpty()
      }
    }
  }
}
