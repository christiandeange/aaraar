package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.Assets
import sh.christian.aaraar.model.FileSet

/**
 * Standard file-wise implementation for merging multiple `assets/` folders.
 */
class AssetsMerger(
  private val fileSetMerger: Merger<FileSet>,
) : Merger<Assets> {
  override fun merge(first: Assets, others: List<Assets>): Assets {
    return Assets(fileSetMerger.merge(first.files, others.map { it.files }))
  }
}
