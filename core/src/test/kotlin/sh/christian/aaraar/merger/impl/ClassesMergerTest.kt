package sh.christian.aaraar.merger.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.externalLibsPath
import sh.christian.aaraar.utils.foo2JarPath
import sh.christian.aaraar.utils.fooJarPath
import java.nio.file.Path
import kotlin.test.Test

class ClassesMergerTest {

  private val merger = ClassesMerger(GenericJarArchiveMerger(MergeRules.None))

  @Test
  fun `nothing to merge`() {
    val animalClasses = animalJarPath.loadClasses()
    val classpath = merger.merge(animalClasses, emptyList())

    with(classpath.archive) {
      this shouldHaveSize 3
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
    }
  }

  @Test
  fun `simple merge with classes`() {
    val animalClasses = animalJarPath.loadClasses()
    val fooClasses = fooJarPath.loadClasses()
    val classpath = merger.merge(animalClasses, fooClasses)

    with(classpath.archive) {
      this shouldHaveSize 4
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/example/Foo.class"
    }
  }

  @Test
  fun `simple merge with libs`() {
    val animalClasses = animalJarPath.loadClasses()
    val externalLibs = Libs.from(externalLibsPath)
    val classpath = merger.merge(animalClasses, externalLibs)

    with(classpath.archive) {
      this shouldHaveSize 5
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/example/Foo.class"
      this shouldHaveKey "com/external/Library.class"
    }
  }

  @Test
  fun `merge with classes with conflicting class files fails`() {
    val fooClasses = fooJarPath.loadClasses()
    val foo2Classes = foo2JarPath.loadClasses()

    shouldThrow<IllegalStateException> {
      merger.merge(fooClasses, foo2Classes)
    }
  }

  @Test
  fun `merge with libs with conflicting class files fails`() {
    val foo2Classes = foo2JarPath.loadClasses()
    val fooLibs = Libs.from(externalLibsPath)

    shouldThrow<IllegalStateException> {
      merger.merge(foo2Classes, fooLibs)
    }
  }

  private fun Path.loadClasses(): Classes {
    return Classes(GenericJarArchive.from(this, keepMetaFiles = true)!!)
  }
}
