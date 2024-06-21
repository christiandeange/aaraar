package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.GenericJarArchive

/**
 * Unsupported implementation for merging multiple `jar` files.
 */
object NoJarArchiveMerger : Merger<GenericJarArchive> {
  override fun merge(first: GenericJarArchive, others: List<GenericJarArchive>): GenericJarArchive {
    error("Merging JARs in this context is not supported.")
  }
}
