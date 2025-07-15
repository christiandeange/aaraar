package sh.christian.aaraar.shading.impl.jarjar.transform.asm

import org.objectweb.asm.commons.Remapper
import sh.christian.aaraar.shading.impl.jarjar.transform.config.ClassRename
import sh.christian.aaraar.shading.impl.jarjar.util.ClassNameUtils

internal class PackageRemapper(
  val patterns: List<ClassRename>,
) : Remapper() {
  private val typeCache: MutableMap<String, String> = mutableMapOf()
  private val pathCache: MutableMap<String, String> = mutableMapOf()
  private val valueCache: MutableMap<Any?, String> = mutableMapOf()

  constructor(vararg patterns: ClassRename) : this(patterns.toList())

  override fun map(key: String): String {
    return typeCache.getOrPut(key) {
      val mapped = replaceHelper(key)
      if (key == mapped) return mapped
      mapped
    }
  }

  override fun mapValue(value: Any?): Any? {
    return if (value is String) {
      valueCache.getOrPut(value) {
        if (ClassNameUtils.isArrayForName(value)) {
          value.replace('.', '/').let(::mapDesc).replace('/', '.')
        } else {
          var s = mapPath(value)
          if (s == value) {
            val hasDot = '.' in s
            val hasSlash = '/' in s
            if (!hasDot || !hasSlash) {
              s = if (hasDot) {
                s.replace('.', '/').let(::replaceHelper).replace('/', '.')
              } else {
                replaceHelper(s)
              }
            }
          }
          s
        }
      }
    } else {
      super.mapValue(value)
    }
  }

  fun mapPath(path: String): String {
    return pathCache.getOrPut(path) {
      var (s, end) = if ('/' !in path) {
        RESOURCE_SUFFIX to path
      } else {
        path.substringBeforeLast("/") + "/$RESOURCE_SUFFIX" to path.substringAfterLast("/")
      }

      s = if (s.startsWith("/")) {
        // Map the path without the leading slash that makes it absolute
        s.substring(1).let(::replaceHelper).let { "/$it" }
      } else {
        replaceHelper(s)
      }

      if (RESOURCE_SUFFIX !in s) {
        path
      } else {
        s.removeSuffix(RESOURCE_SUFFIX) + end
      }
    }
  }

  private fun replaceHelper(value: String): String {
    return patterns.firstNotNullOfOrNull { it.replace(value) } ?: value
  }

  private companion object {
    const val RESOURCE_SUFFIX = "RESOURCE"
  }
}
