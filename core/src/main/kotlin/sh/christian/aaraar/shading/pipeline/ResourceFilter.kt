package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.jarjar.transform.Transformable
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor.Result.DISCARD
import sh.christian.aaraar.shading.impl.jarjar.transform.jar.JarProcessor.Result.KEEP
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils.EXT_CLASS
import sh.christian.aaraar.utils.div
import java.nio.file.FileSystems

internal class ResourceFilter(
  private val resourceDeletes: Set<String>,
) : JarProcessor {
  private val fs = FileSystems.getDefault()

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    val matchingRules = resourceDeletes.filter { fs.getPathMatcher("glob:$it").matches(fs / struct.name) }

    return when {
      // If there are no matching rules, keep the file.
      resourceDeletes.isEmpty() -> KEEP

      // If there are no rules at all, keep the file.
      matchingRules.isEmpty() -> KEEP

      // If the file is a class, only allow removing it if it matches a rule that explicitly ends with ".class".
      // This prevents accidental deletion of classes that match an overly aggressive glob pattern, especially one that
      // may have been configured by default from AGP.
      // https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:build-system/gradle-core/src/main/java/com/android/build/gradle/internal/packaging/PackagingOptionsUtils.kt;l=1?q=PackagingOptionsUtils.kt%20%20&sq=
      ClassNameUtils.isClass(struct.name) -> {
        if (matchingRules.any { it.endsWith(EXT_CLASS) }) {
          DISCARD
        } else {
          KEEP
        }
      }

      // Otherwise, we have at least one matching rule that applies to a resource file, so we discard it.
      else -> DISCARD
    }
  }
}
