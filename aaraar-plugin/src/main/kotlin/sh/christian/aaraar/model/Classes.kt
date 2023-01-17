package sh.christian.aaraar.model

import com.tonicsystems.jarjar.transform.jar.JarProcessorChain
import sh.christian.aaraar.shading.ClassFilesProcessor
import sh.christian.aaraar.shading.ClassFilter
import sh.christian.aaraar.shading.ClassShader
import sh.christian.aaraar.shading.DirectoryFilter
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
    val processor = JarProcessorChain().apply {
      add(DirectoryFilter)
      add(ClassFilter(classDeletes))
      add(ClassShader(classRenames))
    }

    val newArchiveEntries = ClassFilesProcessor(processor).process(archive)

    return Classes(GenericJarArchive.from(newArchiveEntries))
  }

  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(path: Path): Classes {
      return GenericJarArchive.from(path)
        ?.let { archive -> Classes(archive) }
        ?: Classes(GenericJarArchive.NONE)
    }
  }
}
