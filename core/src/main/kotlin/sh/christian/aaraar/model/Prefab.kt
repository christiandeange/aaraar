package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the Prefab package in the `prefab/` folder.
 *
 * AAR files may contain at most one Prefab package. Its package metadata, module metadata, headers, and native
 * libraries are retained as a single file set so they can be consumed by the Android Gradle Plugin.
 */
data class Prefab(
  val files: FileSet,
) {
  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(path: Path): Prefab {
      return FileSet.fromFileTree(path)
        ?.let { files -> Prefab(files) }
        ?: Prefab(files = FileSet.EMPTY)
    }
  }
}
