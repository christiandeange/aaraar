package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.ArchiveMerger
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.JarArchive

/**
 * Composite archive file merger that delegates to either an `.aar` file merger or a `.jar` file merger.
 */
class ArtifactArchiveMerger(
  private val jarArchiveMerger: ArchiveMerger<JarArchive>,
  private val aarArchiveMerger: ArchiveMerger<AarArchive>,
) : ArchiveMerger<ArtifactArchive> {
  override fun merge(first: ArtifactArchive, others: List<ArtifactArchive>): ArtifactArchive {
    return when (first) {
      is JarArchive -> jarArchiveMerger.merge(first, others)
      is AarArchive -> aarArchiveMerger.merge(first, others)
    }
  }
}
