package sh.christian.aaraar.merger

import sh.christian.aaraar.merger.MergeResult.Conflict
import sh.christian.aaraar.merger.MergeResult.MergedContents
import sh.christian.aaraar.merger.MergeResult.PickFirst
import sh.christian.aaraar.merger.MergeResult.Skip
import sh.christian.aaraar.model.GenericJarArchive

internal fun mergeContents(
  original: Map<String, ByteArray>,
  dependencies: List<Map<String, ByteArray>>,
  jarMerger: Merger<GenericJarArchive>,
  mergeRules: MergeRules,
): Map<String, ByteArray> {
  val mergeConflictKeys = mutableSetOf<String>()

  val newEntries: Map<String, ByteArray> = buildMap {
    original
      .filterKeys { it !in mergeRules.excludes }
      .forEach { (name, contents) -> put(name, contents) }

    dependencies
      .flatMap { it.entries }
      .forEach { (name, contents) ->
        val existingContents = this[name]
        if (existingContents != null) {
          when (val result = merge(name, existingContents, contents, mergeRules, jarMerger)) {
            is Conflict -> mergeConflictKeys += name
            is MergedContents -> put(name, result.contents)
            is PickFirst -> putIfAbsent(name, result.contents)
            is Skip -> remove(name)
          }
        } else if (name !in mergeRules.excludes) {
          put(name, contents)
        }
      }
  }

  check(mergeConflictKeys.isEmpty()) {
    "Found differing files when merging: $mergeConflictKeys"
  }

  return newEntries
}

private fun merge(
  entry: String,
  contents1: ByteArray,
  contents2: ByteArray,
  mergeRules: MergeRules,
  jarMerger: Merger<GenericJarArchive>,
): MergeResult {
  return when {
    entry in mergeRules.pickFirsts -> PickFirst(contents1)
    entry in mergeRules.merges -> MergedContents(contents1 + contents2)
    entry in mergeRules.excludes -> Skip
    contents1.contentEquals(contents2) -> MergedContents(contents1)
    entry.startsWith("META-INF/services/") -> {
      MergedContents(
        (contents1.decodeToString() + "\n" + contents2.decodeToString()).trim().encodeToByteArray()
      )
    }

    entry.substringAfterLast('.') == "jar" -> {
      val archive1 = GenericJarArchive.from(contents1, keepMetaFiles = true)
      val archive2 = GenericJarArchive.from(contents2, keepMetaFiles = true)
      when {
        archive1 == null -> MergedContents(contents2)
        archive2 == null -> MergedContents(contents1)
        else -> MergedContents(jarMerger.merge(archive1, archive2).bytes())
      }
    }

    else -> Conflict
  }
}

internal sealed class MergeResult {
  object Skip : MergeResult()
  object Conflict : MergeResult()
  class MergedContents(val contents: ByteArray) : MergeResult()
  class PickFirst(val contents: ByteArray) : MergeResult()
}
