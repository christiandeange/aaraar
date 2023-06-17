package sh.christian.aaraar.model

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors.toList

class Proguard
internal constructor(
  private val lines: List<String>,
) : Mergeable<Proguard>, List<String> by lines {
  override fun plus(others: List<Proguard>): Proguard {
    return Proguard(lines + others.flatMap { it.lines })
  }

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
