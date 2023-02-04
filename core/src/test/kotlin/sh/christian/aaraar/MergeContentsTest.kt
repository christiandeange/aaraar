package sh.christian.aaraar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContainExactly
import sh.christian.aaraar.model.mergeContents
import kotlin.test.Test

class MergeContentsTest {

  @Test
  fun `simple flat merge`() {
    val contents1 = mapOf("foo.txt" to "foo")
    val contents2 = mapOf("bar.txt" to "bar")

    val mergedContents = merge(contents1, listOf(contents2))

    mergedContents.shouldBeExactly(
      "foo.txt" to "foo",
      "bar.txt" to "bar",
    )
  }

  @Test
  fun `simple hierarchical merge`() {
    val contents1 = mapOf("1/foo.txt" to "foo")
    val contents2 = mapOf("1/2/bar.txt" to "bar")

    val mergedContents = merge(contents1, listOf(contents2))

    mergedContents.shouldBeExactly(
      "1/foo.txt" to "foo",
      "1/2/bar.txt" to "bar",
    )
  }

  @Test
  fun `merging two files with same name and contents is okay`() {
    val contents1 = mapOf("foo.txt" to "foo")
    val contents2 = mapOf("foo.txt" to "foo")

    val mergedContents = merge(contents1, listOf(contents2))

    mergedContents.shouldBeExactly(
      "foo.txt" to "foo",
    )
  }

  @Test
  fun `merging two files with same name and different contents fails`() {
    val contents1 = mapOf("file.txt" to "foo")
    val contents2 = mapOf("file.txt" to "bar")

    shouldThrow<IllegalStateException> {
      merge(contents1, listOf(contents2))
    }
  }

  private fun merge(
    original: Map<String, String>,
    dependencies: List<Map<String, String>>,
  ): Map<String, ByteArray> {
    return mergeContents(
      original = original.mapValues { it.value.encodeToByteArray() },
      dependencies = dependencies.map { dependency ->
        dependency.mapValues { it.value.encodeToByteArray() }
      },
    )
  }

  private fun Map<String, ByteArray>.shouldBeExactly(vararg contents: Pair<String, String>) {
    require(contents.isNotEmpty()) {
      "At least one file with contents must be specified!"
    }
    mapValues { it.value.decodeToString() } shouldContainExactly contents.toMap()
  }
}
