package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the set of `jar` files in the `libs/` folder.
 */
class Libs
internal constructor(
  val files: FileSet,
) {
  fun jars(): Map<String, GenericJarArchive> {
    return files.mapNotNull { (path, contents) ->
      if (path.substringAfterLast('.') == "jar") {
        GenericJarArchive.from(contents, keepMetaFiles = true)?.let { path to it }
      } else {
        null
      }
    }.toMap()
  }

  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    val EMPTY = Libs(files = FileSet.EMPTY)

    fun from(path: Path): Libs {
      return FileSet.fromFileTree(path)
        ?.let { files -> Libs(files) }
        ?: EMPTY
    }
  }
}
