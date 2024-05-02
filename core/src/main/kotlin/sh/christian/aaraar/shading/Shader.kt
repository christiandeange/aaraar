package sh.christian.aaraar.shading

import sh.christian.aaraar.model.ShadeConfiguration

/**
 * Used for implementations that apply a [ShadeConfiguration] to a source value to produce a shaded output.
 */
interface Shader<T> {
  fun shade(
    source: T,
    shadeConfiguration: ShadeConfiguration,
  ): T
}
