package sh.christian.aaraar.model.classeditor

import sh.christian.aaraar.model.GenericJarArchive

internal inline fun withClasspath(
  jar: GenericJarArchive = GenericJarArchive.NONE,
  crossinline block: (Classpath) -> Unit,
) = block(Classpath.from(jar))
