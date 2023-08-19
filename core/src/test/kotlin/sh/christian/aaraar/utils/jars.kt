package sh.christian.aaraar.utils

import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import java.nio.file.Path

fun Path.loadJar(): GenericJarArchive {
  return GenericJarArchive.from(this, keepMetaFiles = true) ?: GenericJarArchive.NONE
}

fun GenericJarArchive.shaded(
  classRenames: Map<String, String> = emptyMap(),
  classDeletes: Set<String> = emptySet(),
  resourceDeletes: Set<String> = emptySet(),
): GenericJarArchive {
  return shaded(ShadeConfiguration(classRenames, classDeletes, resourceDeletes))
}
