package sh.christian.aaraar.merger.impl

import io.kotest.assertions.throwables.shouldThrow
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.foo2JarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shouldContainExactly
import kotlin.test.Test

class GenericJarArchiveMergerTest {

  private val merger = GenericJarArchiveMerger(MergeRules.None)

  @Test
  fun `nothing to merge`() {
    val animalClasses = animalJarPath.loadJar()

    merger.merge(animalClasses, emptyList()).shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
    )
  }

  @Test
  fun `simple merge with classes`() {
    val animalClasses = animalJarPath.loadJar()
    val fooClasses = fooJarPath.loadJar()

    merger.merge(animalClasses, fooClasses).shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
      "com/example/Foo.class",
    )
  }

  @Test
  fun `merge with self is redundant`() {
    val fooClasses1 = fooJarPath.loadJar()
    val fooClasses2 = fooJarPath.loadJar()

    merger.merge(fooClasses1, fooClasses2).shouldContainExactly(
      "com/example/Foo.class",
    )
  }

  @Test
  fun `merge with classes with conflicting class files fails`() {
    val fooClasses = fooJarPath.loadJar()
    val foo2Classes = foo2JarPath.loadJar()

    shouldThrow<IllegalStateException> {
      merger.merge(fooClasses, foo2Classes)
    }
  }
}
