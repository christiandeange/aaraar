package sh.christian.aaraar.model

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class Proguard
private constructor(
  private val lines: List<String>,
) : Mergeable<Proguard> {
  override fun plus(others: List<Proguard>): Proguard {
    return Proguard(lines + others.flatMap { it.lines })
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

      val lines = Files.lines(path).toList()
      return Proguard(lines)
    }
  }
}
