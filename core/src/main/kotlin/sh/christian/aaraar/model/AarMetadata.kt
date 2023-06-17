package sh.christian.aaraar.model

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors.toList

data class AarMetadata(
  private val lines: List<String>,
) {
  fun writeTo(path: Path) {
    if (lines.isEmpty()) {
      Files.deleteIfExists(path)
    } else {
      Files.write(path, lines)
    }
  }

  companion object {
    fun from(path: Path): AarMetadata {
      if (!Files.isRegularFile(path)) return AarMetadata(lines = emptyList())

      val lines = Files.lines(path).collect(toList())
      return AarMetadata(lines)
    }
  }
}
