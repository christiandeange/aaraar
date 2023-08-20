package sh.christian.aaraar.merger

import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.JarArchive

class JarArchiveMerger(
  private val classesMerger: Merger<Classes>,
) : ArchiveMerger<JarArchive> {
  override fun merge(first: JarArchive, others: List<ArtifactArchive>): JarArchive {
    val mergedClasses = classesMerger.merge(
      first.classes,
      others.map { it.classes },
    )

    // Generally speaking a module producing a JAR should only have other JARs as dependencies.
    // However, even if this is not true (ie: somehow a JAR depends on an AAR), we will silently
    // throw away all the other AAR entries and publish the file merged archive as a JAR as well.
    return JarArchive(classes = mergedClasses)
  }
}
