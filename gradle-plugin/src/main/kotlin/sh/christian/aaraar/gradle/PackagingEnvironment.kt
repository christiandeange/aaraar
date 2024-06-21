package sh.christian.aaraar.gradle

import java.io.Serializable

/**
 * Defines the environment of all applicable packaging rules.
 */
data class PackagingEnvironment(
  /**
   * Packaging rules for merging JNI library folders.
   */
  val jniLibs: JniLibs,
  /**
   * Packaging rules for merging resource files.
   */
  val resources: Resources,
) : Serializable {

  data class JniLibs(
    /**
     * The excluded pattern(s).
     */
    val excludes: Set<String>,
    /**
     * The pattern(s) for which the first occurrence is packaged. Ordering is determined by the order of dependencies.
     */
    val pickFirsts: Set<String>,
  ) : Serializable {
    companion object {
      private const val serialVersionUID = 1L
    }
  }

  data class Resources(
    /**
     * The excluded pattern(s).
     */
    val excludes: Set<String>,
    /**
     * The pattern(s) for which the first occurrence is packaged. Ordering is determined by the order of dependencies.
     */
    val pickFirsts: Set<String>,
    /**
     * The pattern(s) for which matching resources are merged into a single entry.
     */
    val merges: Set<String>,
  ) : Serializable {
    companion object {
      private const val serialVersionUID = 1L
    }
  }

  companion object {
    private const val serialVersionUID = 1L

    /**
     * Sentinel value for no custom packaging rules.
     */
    val None = PackagingEnvironment(
      jniLibs = JniLibs(
        excludes = emptySet(),
        pickFirsts = emptySet(),
      ),
      resources = Resources(
        excludes = emptySet(),
        pickFirsts = emptySet(),
        merges = emptySet(),
      ),
    )
  }
}
