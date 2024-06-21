package sh.christian.aaraar.merger

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import sh.christian.aaraar.model.GenericJarArchive
import kotlin.test.Test

class MergeContentsTest {

  private val jarMerger = object : Merger<GenericJarArchive> {
    override fun merge(first: GenericJarArchive, others: List<GenericJarArchive>) = error("Not implemented")
  }

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

  @Test
  fun `merge rules - pickFirsts`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(pickFirsts = Glob.fromString("*.txt")),
    )

    mergedContents.shouldBeExactly(
      "file.txt" to "contents1",
    )
  }

  @Test
  fun `merge rules - merges`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(merges = Glob.fromString("*.txt")),
    )

    mergedContents.shouldBeExactly(
      "file.txt" to "contents1contents2",
    )
  }

  @Test
  fun `merge rules - merges with newlines if present`() {
    val contents1 = mapOf("file.txt" to "contents1\n")
    val contents2 = mapOf("file.txt" to "contents2\n")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(merges = Glob.fromString("*.txt")),
    )

    mergedContents.shouldBeExactly(
      "file.txt" to "contents1\ncontents2\n",
    )
  }

  @Test
  fun `merge rules - excludes`() {
    val contents1 = mapOf("file.txt" to "contents1", "other.md" to "readme")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(excludes = Glob.fromString("*.txt")),
    )

    mergedContents.shouldBeExactly(
      "other.md" to "readme",
    )
  }

  @Test
  fun `merge rules - excludes results in no files`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(excludes = Glob.fromString("*.txt")),
    )

    mergedContents.shouldBeEmpty()
  }

  @Test
  fun `merge rules - excludes even if no conflict`() {
    val contents1 = mapOf("file.txt" to "contents")
    val contents2 = mapOf("other.md" to "readme")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(excludes = Glob.fromString("*.txt")),
    )

    mergedContents.shouldBeExactly(
      "other.md" to "readme",
    )
  }

  @Test
  fun `merge rules priority - exclude over all`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(
        pickFirsts = Glob.fromString("*.txt"),
        merges = Glob.fromString("*.txt"),
        excludes = Glob.fromString("*.txt"),
      )
    )

    mergedContents.shouldBeEmpty()
  }

  @Test
  fun `merge rules priority - pickfirst over merge`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(
        pickFirsts = Glob.fromString("*.txt"),
        merges = Glob.fromString("*.txt"),
      )
    )

    mergedContents.shouldBeExactly(
      "file.txt" to "contents1",
    )
  }

  @Test
  fun `merge rules priority - merge over conflict`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    val mergedContents = merge(
      original = contents1,
      dependencies = listOf(contents2),
      mergeRules = mergeRules(
        merges = Glob.fromString("*.txt"),
      )
    )

    mergedContents.shouldBeExactly(
      "file.txt" to "contents1contents2",
    )
  }

  @Test
  fun `merge rules priority - conflict if no rules defined`() {
    val contents1 = mapOf("file.txt" to "contents1")
    val contents2 = mapOf("file.txt" to "contents2")

    shouldThrow<IllegalStateException> {
      merge(
        original = contents1,
        dependencies = listOf(contents2),
        mergeRules = mergeRules(),
      )
    }
  }

  private fun merge(
    original: Map<String, String>,
    dependencies: List<Map<String, String>>,
    mergeRules: MergeRules = MergeRules.None,
  ): Map<String, ByteArray> {
    return mergeContents(
      original = original.mapValues { it.value.encodeToByteArray() },
      dependencies = dependencies.map { dependency ->
        dependency.mapValues { it.value.encodeToByteArray() }
      },
      jarMerger = jarMerger,
      mergeRules = mergeRules,
    )
  }

  private fun mergeRules(
    pickFirsts: Glob = Glob.None,
    merges: Glob = Glob.None,
    excludes: Glob = Glob.None,
  ): MergeRules {
    return MergeRules(
      pickFirsts = pickFirsts,
      merges = merges,
      excludes = excludes,
    )
  }

  private fun Map<String, ByteArray>.shouldBeExactly(vararg contents: Pair<String, String>) {
    require(contents.isNotEmpty()) {
      "At least one file with contents must be specified!"
    }
    mapValues { it.value.decodeToString() } shouldContainExactly contents.toMap()
  }
}
