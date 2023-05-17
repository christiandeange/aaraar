package sh.christian.aaraar.gradle.agp

/**
 * A facade of some of the interactions with the `android` extension on an Android module.
 */
interface AndroidExtension {
  /**
   * Allows for registration of a callback to be called with build type names.
   */
  fun onBuildTypes(callback: (String) -> Unit)

  /**
   * Returns the list of wildcards that will be used to exclude files from the final packaged aar.
   */
  fun packagingResourceExcludes(): Set<String>
}
