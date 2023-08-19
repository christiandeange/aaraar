package sh.christian.aaraar.merger

import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.GenericJarArchive

class FileSetMerger(
  private val jarMerger: Merger<GenericJarArchive>,
) : Merger<FileSet> {
  override fun merge(first: FileSet, others: List<FileSet>): FileSet {
    return FileSet(mergeContents(first, others, jarMerger))
  }
}
