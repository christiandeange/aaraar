package sh.christian.aaraar.merger

import sh.christian.aaraar.model.Assets
import sh.christian.aaraar.model.FileSet

class AssetsMerger(
  private val fileSetMerger: Merger<FileSet>,
) : Merger<Assets> {
  override fun merge(first: Assets, others: List<Assets>): Assets {
    return Assets(fileSetMerger.merge(first.files, others.map { it.files }))
  }
}
