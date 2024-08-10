package sh.christian.aaraar.utils

import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.classeditor.Classpath
import sh.christian.aaraar.model.classeditor.MutableClasspath
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun withClasspath(
  jar: GenericJarArchive = GenericJarArchive.NONE,
  crossinline block: (MutableClasspath) -> Unit,
) {
  contract {
    callsInPlace(block, EXACTLY_ONCE)
  }
  block(MutableClasspath.from(jar))
}
