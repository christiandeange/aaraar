package sh.christian.aaraar.shading

import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.impl.GenericJarArchiveShader

internal fun GenericJarArchive.shaded(
  classRenames: Map<String, String> = emptyMap(),
  classDeletes: Set<String> = emptySet(),
  resourceRenames: Map<String, String> = emptyMap(),
  resourceDeletes: Set<String> = emptySet(),
): GenericJarArchive {
  return GenericJarArchiveShader().shade(
    source = this,
    shadeConfiguration = ShadeConfiguration(
      classRenames = classRenames,
      classDeletes = classDeletes,
      resourceRenames = resourceRenames,
      resourceDeletes = resourceDeletes,
    ),
  )
}
