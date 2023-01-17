package sh.christian.aaraar.model

import java.nio.file.Path

class LintRules
private constructor(
  private val archive: GenericJarArchive,
) : Mergeable<LintRules> {
  override fun plus(others: List<LintRules>): LintRules {
    return LintRules(archive + others.map { it.archive })
  }

  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(path: Path): LintRules {
      return GenericJarArchive.from(path, keepMetaFiles = true)
        ?.let { archive -> LintRules(archive) }
        ?: LintRules(GenericJarArchive.NONE)
    }
  }
}
