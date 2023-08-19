package sh.christian.aaraar.merger

import sh.christian.aaraar.model.GenericJarArchive

class GenericJarArchiveMerger : Merger<GenericJarArchive> {
  override fun merge(first: GenericJarArchive, others: List<GenericJarArchive>): GenericJarArchive {
    return GenericJarArchive(mergeContents(first, others, this))
  }
}
