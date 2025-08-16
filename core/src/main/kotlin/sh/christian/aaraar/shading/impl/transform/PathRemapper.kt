package sh.christian.aaraar.shading.impl.transform

import org.objectweb.asm.commons.Remapper

internal class PathRemapper<T>(
  private val patterns: List<T>,
) : Remapper() where T : AbstractPattern, T : ReplacePattern {
  private val typeCache: MutableMap<String, String> = mutableMapOf()
  private val pathCache: MutableMap<String, String> = mutableMapOf()
  private val valueCache: MutableMap<Any?, String> = mutableMapOf()

  constructor(vararg patterns: T) : this(patterns.toList())

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
        value.let(::mapPath).let(::replaceHelper)
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
