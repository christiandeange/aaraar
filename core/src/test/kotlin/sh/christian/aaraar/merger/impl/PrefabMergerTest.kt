package sh.christian.aaraar.merger.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContainExactly
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.Prefab
import kotlin.test.Test

class PrefabMergerTest {

  private val merger = PrefabMerger(
    FileSetMerger(NoJarArchiveMerger, MergeRules.None),
  )

  @Test
  fun `merges non-overlapping modules from one Prefab package`() {
    val first = Prefab(
      FileSet(
        mapOf(
          "prefab.json" to "{\"name\":\"example\"}".encodeToByteArray(),
          "modules/foo/module.json" to "{}".encodeToByteArray(),
        ),
      ),
    )
    val second = Prefab(
      FileSet(
        mapOf(
          "prefab.json" to "{\"name\":\"example\"}".encodeToByteArray(),
          "modules/bar/module.json" to "{}".encodeToByteArray(),
        ),
      ),
    )

    val merged = merger.merge(first, second)

    merged.files.mapValues { it.value.decodeToString() }.shouldContainExactly(
      mapOf(
        "prefab.json" to "{\"name\":\"example\"}",
        "modules/foo/module.json" to "{}",
        "modules/bar/module.json" to "{}",
      ),
    )
  }

  @Test
  fun `conflicting Prefab package metadata fails`() {
    val first = Prefab(FileSet(mapOf("prefab.json" to "{\"name\":\"first\"}".encodeToByteArray())))
    val second = Prefab(FileSet(mapOf("prefab.json" to "{\"name\":\"second\"}".encodeToByteArray())))

    shouldThrow<IllegalStateException> {
      merger.merge(first, second)
    }
  }
}
