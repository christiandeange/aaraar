package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.merger.mergeContents
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.GenericJarArchive

/**
 * Standard file-wise implementation for merging multiple sets of files.
 *
 * If there are any duplicate file paths with differing file contents, an exception will be thrown.
 */
class FileSetMerger(
  private val jarMerger: Merger<GenericJarArchive>,
) : Merger<FileSet> {
  override fun merge(first: FileSet, others: List<FileSet>): FileSet {
    return FileSet(mergeContents(first, others, jarMerger))
  }
}
