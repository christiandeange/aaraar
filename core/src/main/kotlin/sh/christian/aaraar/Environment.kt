package sh.christian.aaraar

import java.io.Serializable

/**
 * General properties that influence the archive merging process.
 *
 * @param androidAaptIgnore the value of the `ANDROID_AAPT_IGNORE` environment variable.
 * @param keepClassesMetaFiles whether `META-INF/` files should be kept in merged archive file.
 */
data class Environment(
  val androidAaptIgnore: String,
  val keepClassesMetaFiles: Boolean,
) : Serializable {
  companion object {
    private const val serialVersionUID = 1L
  }
}
