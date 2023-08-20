package sh.christian.aaraar.merger

import sh.christian.aaraar.model.ArtifactArchive

interface ArchiveMerger<T : ArtifactArchive> {
  fun merge(first: T, others: List<ArtifactArchive>): T
}
