package sh.christian.aaraar.model

import com.tonicsystems.jarjar.classpath.ClassPath
import com.tonicsystems.jarjar.transform.JarTransformer
import com.tonicsystems.jarjar.transform.config.ClassDelete
import com.tonicsystems.jarjar.transform.config.ClassRename
import com.tonicsystems.jarjar.transform.jar.DefaultJarProcessor
import sh.christian.aaraar.utils.deleteIfExists
import java.nio.file.Files
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
    val inputJar = Files.createTempFile("classes", ".jar").deleteIfExists()
    val outputJar = Files.createTempFile("classes", ".jar").deleteIfExists()
    writeTo(inputJar)

    val processor = DefaultJarProcessor().apply {
      classRenames.forEach { (pattern, result) ->
        addClassRename(ClassRename(pattern, result))
      }
      classDeletes.forEach { pattern ->
        addClassDelete(ClassDelete(pattern))
      }
    }

    val inputJarFile = inputJar.toFile()
    val outputJarFile = outputJar.toFile()
    val classpath = ClassPath(
      /* root */ inputJarFile.parentFile,
      /* entries */ arrayOf(inputJarFile.relativeTo(inputJarFile.parentFile))
    )

    JarTransformer(outputJarFile, processor).transform(classpath)

    return from(outputJar)
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
