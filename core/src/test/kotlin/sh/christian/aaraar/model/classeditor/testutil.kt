package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.metadata.KmClass

internal val MutableClasspath.foo get() = this["sh.christian.mylibrary.Foo"]
internal val MutableClasspath.fooInternal get() = this["sh.christian.mylibrary.FooInternal"]
internal val MutableClasspath.name get() = this["sh.christian.mylibrary.Name"]

internal fun MutableClassReference.requireMetadata(): KmClass {
  return kotlinMetadata.shouldNotBeNull().kmClass
}
