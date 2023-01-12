package sh.christian.aaraar.model

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class Proguard
private constructor(
  private val lines: List<String>,
) {
  operator fun plus(other: Proguard): Proguard {
    return Proguard(lines + other.lines)
  }

  companion object {
    fun from(path: Path): Proguard {
      if (!Files.isRegularFile(path)) return Proguard(lines = emptyList())

      val lines = Files.lines(path).toList()
      return Proguard(lines)
    }
  }
}
