package sh.christian.aaraar.utils

import sh.christian.aaraar.model.GenericJarArchive
import java.nio.file.Path

fun Path.loadJar(): GenericJarArchive {
  return GenericJarArchive.from(this, keepMetaFiles = true) ?: GenericJarArchive.NONE
}
