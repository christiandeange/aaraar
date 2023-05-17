package sh.christian.aaraar.gradle.agp

import org.gradle.api.attributes.AttributeContainer

/**
 * Compatibility layer for interacting with the Android Gradle Plugin from the Aaraar plugin.
 *
 * We cannot predict exactly which version of AGP will be on our classpath at runtime, and we want
 * to be able to support at least a few different major versions at a time, some of which may be
 * binary-incompatible with one another and have breaking API changes.
 *
 * Because of this, we need to create a facade in front of any AGP-defined type, using our own
 * classes in order to create the same kind of behaviour across different AGP versions.
 */
interface AgpCompat {
  /**
   * Returns a shim of the `android` extension registered on an Android module.
   */
  val android: AndroidExtension

  /**
   * Sets the build type attribute on an attributable object with the given build type name.
   */
  fun AttributeContainer.buildTypeAttribute(buildType: String)

  /**
   * Allows for registration of a callback to be called with variant instances.
   * This can be used to modify compilation behaviour of a given variant at configuration time.
   */
  fun onVariants(callback: (AndroidVariant) -> Unit)
}
