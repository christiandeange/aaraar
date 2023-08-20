package sh.christian.aaraar.model

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors.toList

/**
 * Represents the set of consumer Proguard files.
 */
class Proguard
internal constructor(
  val lines: List<String>,
) : List<String> by lines {
  override fun toString(): String {
    return lines.joinToString(separator = "\n")
  }

  fun writeTo(path: Path) {
    if (lines.isEmpty()) {
      Files.deleteIfExists(path)
    } else {
      Files.write(path, lines)
    }
  }

  companion object {
    fun from(path: Path): Proguard {
      if (!Files.isRegularFile(path)) return Proguard(lines = emptyList())

      val lines = Files.lines(path).collect(toList())
      return Proguard(lines)
    }
  }
}
