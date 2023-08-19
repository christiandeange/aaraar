package sh.christian.aaraar.model

import java.nio.file.Path

class LintRules
internal constructor(
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
