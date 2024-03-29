package sh.christian.aaraar.merger

internal sealed class MergeResult {
  object Skip : MergeResult()
  object Conflict : MergeResult()
  class MergedContents(val contents: ByteArray) : MergeResult()
}
