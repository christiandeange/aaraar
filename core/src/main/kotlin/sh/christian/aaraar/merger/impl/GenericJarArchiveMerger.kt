package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.merger.mergeContents
import sh.christian.aaraar.model.GenericJarArchive

/**
 * Standard jar-wise implementation for merging multiple `jar` files.
 *
 * If there are any duplicate file paths with differing file contents, the following logic will be applied:
 * - If the files have the same file contents, those contents are used.
 * - If the files are in the `META-INF/services/` subfolder, the file contents are appended.
 * - If the files are also `jar` files, they will recursively be merged with this same logic applied.
 * - Otherwise, an exception will be thrown.
 */
class GenericJarArchiveMerger : Merger<GenericJarArchive> {
  override fun merge(first: GenericJarArchive, others: List<GenericJarArchive>): GenericJarArchive {
    return GenericJarArchive(mergeContents(first, others, this))
  }
}
