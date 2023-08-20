package sh.christian.aaraar.merger

import sh.christian.aaraar.model.ArtifactArchive

/**
 * Interface for implementations that merge a set of [ArtifactArchive]s into a specific type of [ArtifactArchive].
 *
 * This is useful for when the merging target has a known sealed type, but the dependencies do not.
 */
interface ArchiveMerger<T : ArtifactArchive> {
  fun merge(first: T, others: List<ArtifactArchive>): T
}
