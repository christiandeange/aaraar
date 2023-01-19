package sh.christian.aaraar

import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.withDirectory
import sh.christian.aaraar.utils.withFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
      fileSet.assertHasNoFiles()
    }
  }

  @Test
  fun `simple flat merge`() {
    val fileSet1 = FileSet.from(mapOf("foo.txt" to "foo"))
    val fileSet2 = FileSet.from(mapOf("bar.txt" to "bar"))

    val mergedFileSet = fileSet1 + fileSet2

    mergedFileSet.assertHasFiles(
      "foo.txt" to "foo",
      "bar.txt" to "bar",
    )
  }

  @Test
  fun `simple hierarchical merge`() {
    val fileSet1 = FileSet.from(mapOf("1/foo.txt" to "foo"))
    val fileSet2 = FileSet.from(mapOf("1/2/bar.txt" to "bar"))

    val mergedFileSet = fileSet1 + fileSet2

    mergedFileSet.assertHasFiles(
      "1/foo.txt" to "foo",
      "1/2/bar.txt" to "bar",
    )
  }

  @Test
  fun `merging two files with same name and contents is okay`() {
    val fileSet1 = FileSet.from(mapOf("foo.txt" to "foo"))
    val fileSet2 = FileSet.from(mapOf("foo.txt" to "foo"))

    val mergedFileSet = fileSet1 + fileSet2

    mergedFileSet.assertHasFiles(
      "foo.txt" to "foo",
    )
  }

  @Test
  fun `merging two files with same name and different contents fails`() {
    val fileSet1 = FileSet.from(mapOf("foo.txt" to "foo"))
    val fileSet2 = FileSet.from(mapOf("foo.txt" to "bar"))

    assertFailsWith<IllegalStateException> {
      fileSet1 + fileSet2
    }
  }

  private fun FileSet.assertHasFiles(vararg filesWithContents: Pair<String, String>) {
    require(filesWithContents.isNotEmpty()) {
      "At least one file with contents must be specified!"
    }
    withDirectory {
      writeTo(root)
      assertEquals(
        expected = filesWithContents.toMap(),
        actual = files(),
      )
    }
  }

  private fun FileSet.assertHasNoFiles() {
    withDirectory {
      writeTo(root)
      assertEquals(
        expected = emptyMap(),
        actual = files(),
      )
    }
  }

  private fun FileSet.Companion.from(fileSet: Map<String, String>): FileSet {
    return from(fileSet.mapValues { it.value.encodeToByteArray() })
  }
}
