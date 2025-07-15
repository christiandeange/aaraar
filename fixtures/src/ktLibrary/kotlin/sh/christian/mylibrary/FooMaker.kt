@file:JvmName("Foos")

package sh.christian.mylibrary

fun newFoo() = Foo()

@JvmField
val twoFoos: Array<Foo> = arrayOf(Foo(), Foo())
