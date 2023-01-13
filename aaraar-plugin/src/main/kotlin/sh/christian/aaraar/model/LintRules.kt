package sh.christian.aaraar.model

import java.nio.file.Path

class LintRules
private constructor(
  private val archive: GenericJarArchive,
) : Mergeable<LintRules> {
  override operator fun plus(other: LintRules): LintRules {
    return LintRules(archive + other.archive)
  }

  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(path: Path): LintRules {
      return GenericJarArchive.from(path)
        ?.let { archive -> LintRules(archive) }
        ?: LintRules(GenericJarArchive.NONE)
    }
  }
}
