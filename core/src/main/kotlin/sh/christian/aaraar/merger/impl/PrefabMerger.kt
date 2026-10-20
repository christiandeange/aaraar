package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.Prefab

/**
 * Standard file-wise implementation for merging Prefab packages.
 *
 * Conflicting package metadata, module metadata, headers, or native libraries fail the merge. This supports retaining
 * a single Prefab package and safely combining non-overlapping modules from identical packages; it does not fuse
 * distinct Prefab packages.
 */
class PrefabMerger(
  private val fileSetMerger: Merger<FileSet>,
) : Merger<Prefab> {
  override fun merge(
    first: Prefab,
    others: List<Prefab>,
  ): Prefab {
    return Prefab(fileSetMerger.merge(first.files, others.map { it.files }))
  }
}
