package sh.christian.aaraar

import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.withFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FileSetTest {

  @Test
  fun `from nonexistent file tree returns null`() {
    withFileSystem {
      assertNull(FileSet.fromFileTree(root / "Documents and Settings"))
    }
  }

  @Test
  fun `from empty file tree returns empty FileSet`() {
    withFileSystem {
      val fileSet = FileSet.fromFileTree(root)
      assertNotNull(fileSet)

      withDirectory {
        fileSet.writeTo(root)
        assertEquals(
          expected = emptyMap(),
          actual = files(),
        )
      }
    }
  }
}
