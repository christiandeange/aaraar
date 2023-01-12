package sh.christian.aaraar.model

import java.nio.file.Path

class LintRules
private constructor(
  private val archive: GenericJarArchive,
) {
  companion object {
    fun from(path: Path): LintRules? {
      return GenericJarArchive.from(path)?.let { archive -> LintRules(archive) }
    }
  }
}
