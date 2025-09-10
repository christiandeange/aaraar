package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the set of consumer Lint rules.
 */
class LintRules(
  val archive: GenericJarArchive,
) {
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
