package sh.christian.aaraar.model

import com.tonicsystems.jarjar.transform.jar.JarProcessorChain
import sh.christian.aaraar.shading.ClassFilesProcessor
import sh.christian.aaraar.shading.ClassFilter
import sh.christian.aaraar.shading.ClassShader
import java.nio.file.Path

class Classes
private constructor(
  private val archive: GenericJarArchive,
) : Mergeable<Classes> {
  override operator fun plus(others: List<Classes>): Classes {
    return Classes(archive + others.map { it.archive })
  }

  fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): Classes {
    return Classes(archive.shaded(classRenames, classDeletes))
  }

  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): Classes {
      return GenericJarArchive.from(path, keepMetaFiles)
        ?.let { archive -> Classes(archive) }
        ?: Classes(GenericJarArchive.NONE)
    }
  }
}
