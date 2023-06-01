package sh.christian.aaraar.gradle

/**
 * Simple definition of an Android variant.
 */
data class VariantDescriptor(
  val name: String,
  val buildType: String?,
)
