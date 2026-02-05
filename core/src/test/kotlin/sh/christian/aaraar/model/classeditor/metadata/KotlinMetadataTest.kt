package sh.christian.aaraar.model.classeditor.metadata

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.classeditor.MutableClassReference
import sh.christian.aaraar.model.classeditor.foo
import sh.christian.aaraar.model.classeditor.fooInternal
import sh.christian.aaraar.model.classeditor.immutable
import sh.christian.aaraar.model.classeditor.requireMetadata
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shouldBe
import sh.christian.aaraar.utils.withClasspath
import kotlin.metadata.KmClassifier
import kotlin.test.Test

class KotlinMetadataTest {

  @Test
  fun `no metadata on compiled class files missing it`() = withClasspath(fooJarPath.loadJar()) { cp ->
    cp.classes.forEach { clazz ->
      clazz.kotlinMetadata.shouldBeNull()
    }
  }

  @Test
  fun `read metadata on public kotlin class file`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    val metadata = cp.foo.requireMetadata()

    metadata.name.toQualifiedName() shouldBe cp.foo.qualifiedName
    metadata.supertypes shouldHaveSize 1
    metadata.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Any"
    }

    metadata.constructors shouldHaveSize 1
    metadata.functions.map { it.name }.shouldContainExactlyInAnyOrder("print", "printInternal")
  }

  @Test
  fun `read metadata on internal kotlin class file`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    val metadata = cp.fooInternal.requireMetadata()

    metadata.name.toQualifiedName() shouldBe cp.fooInternal.qualifiedName
    metadata.supertypes shouldHaveSize 1
    metadata.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Any"
    }

    metadata.constructors shouldHaveSize 1
    metadata.functions.map { it.name } shouldContainExactly listOf("printInternal")
  }

  @Test
  fun `read metadata on annotation class file`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    val metadata = cp.immutable.requireMetadata()

    metadata.name.toQualifiedName() shouldBe cp.immutable.qualifiedName
    metadata.supertypes shouldHaveSize 1
    metadata.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Annotation"
    }

    metadata.constructors shouldHaveSize 1
    metadata.functions.shouldBeEmpty()
  }

  @Test
  fun `metadata loading is stable`() {
    val oldJar = ktLibraryJarPath.loadJar()
    val newJar: GenericJarArchive

    val oldClass: MutableClassReference
    val newClass: MutableClassReference

    withClasspath(oldJar) { cp ->
      oldClass = cp.immutable
      newJar = cp.toGenericJarArchive()
    }

    withClasspath(newJar) { cp ->
      newClass = cp.immutable
    }

    oldClass.requireMetadata() shouldBe newClass.requireMetadata()
  }
}
