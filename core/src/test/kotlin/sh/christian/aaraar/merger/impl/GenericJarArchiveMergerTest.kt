package sh.christian.aaraar.merger.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.foo2JarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.loadJar
import kotlin.test.Test

class GenericJarArchiveMergerTest {

  private val merger = GenericJarArchiveMerger()

  @Test
  fun `simple merge with classes`() {
    val animalClasses = animalJarPath.loadJar()
    val fooClasses = fooJarPath.loadJar()

    with(merger.merge(animalClasses, fooClasses)) {
      this shouldHaveSize 4
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/example/Foo.class"
    }
  }

  @Test
  fun `merge with self is redundant`() {
    val fooClasses1 = fooJarPath.loadJar()
    val fooClasses2 = fooJarPath.loadJar()

    with(merger.merge(fooClasses1, fooClasses2)) {
      this shouldHaveSize 1
      this shouldHaveKey "com/example/Foo.class"
    }
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
