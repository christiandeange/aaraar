package sh.christian.aaraar.model.classeditor

import sh.christian.aaraar.model.GenericJarArchive
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun withClasspath(
  jar: GenericJarArchive = GenericJarArchive.NONE,
  crossinline block: (Classpath) -> Unit,
) {
  contract {
    callsInPlace(block, EXACTLY_ONCE)
  }
  block(Classpath.from(jar))
}
