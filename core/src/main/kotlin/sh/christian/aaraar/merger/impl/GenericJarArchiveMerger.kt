package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.merger.mergeContents
import sh.christian.aaraar.model.GenericJarArchive

class GenericJarArchiveMerger : Merger<GenericJarArchive> {
  override fun merge(first: GenericJarArchive, others: List<GenericJarArchive>): GenericJarArchive {
    return GenericJarArchive(mergeContents(first, others, this))
  }
}
