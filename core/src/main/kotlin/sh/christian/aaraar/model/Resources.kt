package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the contents of the `res/` folder.
 */
data class Resources(
  val files: FileSet,
  val packageName: String,
  val minSdk: Int,
  val androidAaptIgnore: String,
) {
  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(
      path: Path,
      packageName: String,
      minSdk: Int,
      androidAaptIgnore: String,
    ): Resources {
      return FileSet.fromFileTree(path)
        ?.let { files ->
          Resources(
            files = files,
            packageName = packageName,
            minSdk = minSdk,
            androidAaptIgnore = androidAaptIgnore,
          )
        }
        ?: Resources(
          files = FileSet.EMPTY,
          packageName = packageName,
          minSdk = minSdk,
          androidAaptIgnore = androidAaptIgnore,
        )
    }
  }
}
