package sh.christian.aaraar.model

import java.nio.file.Path

class Libs
private constructor(
  private val files: FileSet,
) : Mergeable<Libs> {
  override fun plus(others: List<Libs>): Libs {
    return Libs(files + others.map { it.files })
  }

  fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): Libs {
    val shadedFiles = files.mapValues { (path, contents) ->
      if (path.substringAfterLast('.') == "jar") {
        GenericJarArchive.from(contents, keepMetaFiles = true)
          ?.shaded(classRenames, classDeletes)
          ?.bytes()
          ?: ByteArray(0)
      } else {
        contents
      }
    }

    return Libs(FileSet.from(shadedFiles))
  }

  fun jars(): List<GenericJarArchive> {
    return files.mapNotNull { (path, contents) ->
      if (path.substringAfterLast('.') == "jar") {
        GenericJarArchive.from(contents, keepMetaFiles = true)
      } else {
        null
      }
    }
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
