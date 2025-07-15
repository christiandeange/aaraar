@file:JvmName("FooInternals")

package sh.christian.mylibrary

internal fun newFooInternal() = FooInternal()

@JvmField
internal val twoFooInternals: Array<FooInternal> = arrayOf(FooInternal(), FooInternal())
