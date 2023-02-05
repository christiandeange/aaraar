package sh.christian.aaraar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.GenericJarArchive.Companion.NONE
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.externalLibsPath
import sh.christian.aaraar.utils.foo2JarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.withFile
import java.nio.file.Path
import kotlin.test.Test

class ClassesTest {

  @Test
  fun `simple merge with classes`() {
    val animalClasses = animalJarPath.loadClasses()
    val fooClasses = fooJarPath.loadClasses()

    withClasses(animalClasses + fooClasses) {
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

    withClasses(animalClasses + externalLibs) {
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
      fooClasses + foo2Classes
    }
  }

  @Test
  fun `merge with libs with conflicting class files fails`() {
    val foo2Classes = foo2JarPath.loadClasses()
    val fooLibs = Libs.from(externalLibsPath)

    shouldThrow<IllegalStateException> {
      foo2Classes + fooLibs
    }
  }

  private fun withClasses(
    classes: Classes,
    block: GenericJarArchive.() -> Unit,
  ) {
    withFile {
      filePath.deleteIfExists()
      classes.writeTo(filePath)

      with(GenericJarArchive.from(filePath, true) ?: NONE, block)
    }
  }

  private fun Path.loadClasses(): Classes {
    return Classes(GenericJarArchive.from(this, keepMetaFiles = true)!!)
  }
}
