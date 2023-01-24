package sh.christian.aaraar.model

import sh.christian.aaraar.model.MergeResult.Conflict
import sh.christian.aaraar.model.MergeResult.MergedContents
import sh.christian.aaraar.model.MergeResult.Skip

internal sealed class MergeResult {
  object Skip : MergeResult()
  object Conflict : MergeResult()
  class MergedContents(val contents: ByteArray) : MergeResult()
}

internal fun mergeContents(
  original: Map<String, ByteArray>,
  dependencies: List<Map<String, ByteArray>>,
): Map<String, ByteArray> {
  val mergeConflictKeys = mutableSetOf<String>()

  @OptIn(ExperimentalStdlibApi::class)
  val newEntries: Map<String, ByteArray> = buildMap {
    putAll(original)

    dependencies.flatMap { it.entries }.forEach { (name, contents) ->
      if (name in this) {
        when (val result = merge(name, this[name]!!, contents)) {
          is Conflict -> mergeConflictKeys += name
          is MergedContents -> put(name, result.contents)
          is Skip -> remove(name)
        }
      } else {
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
): MergeResult {
  return when {
    contents1.contentEquals(contents2) -> MergedContents(contents1)
    entry.startsWith("META-INF/services/") -> {
      MergedContents(
        (contents1.decodeToString() + "\n" + contents2.decodeToString()).trim().encodeToByteArray()
      )
    }

    entry.substringAfterLast('.') == "jar" -> {
      val archive1 = GenericJarArchive.from(contents1, keepMetaFiles = true)!!
      val archive2 = GenericJarArchive.from(contents2, keepMetaFiles = true)!!
      MergedContents((archive1 + archive2).bytes())
    }

    else -> Conflict
  }
}
