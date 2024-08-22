package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.metadata.KmClassifier
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.classeditor.metadata.toQualifiedName
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.ktLibraryPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class KotlinMetadataTest {

  @Test
  fun `no metadata on compiled class files missing it`() = withClasspath(fooJarPath.loadJar()) { cp ->
    cp.classes.forEach { clazz ->
      clazz.kotlinMetadata.shouldBeNull()
    }
  }

  @Test
  fun `read metadata on compiled kotlin class files`() = withClasspath(ktLibraryPath.loadJar()) { cp ->
    val foo = cp["sh.christian.mylibrary.Foo"]
    val metadata = foo.kotlinMetadata
    metadata.shouldNotBeNull()

    metadata.kmClass.name.toQualifiedName() shouldBe foo.qualifiedName
    metadata.kmClass.supertypes shouldHaveSize 1
    metadata.kmClass.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Any"
    }

    metadata.kmClass.constructors shouldHaveSize 1
    metadata.kmClass.functions shouldHaveSize 2
    metadata.kmClass.functions.map { it.name } shouldContainExactlyInAnyOrder listOf("print", "printInternal")

    val fooInternal = cp["sh.christian.mylibrary.FooInternal"]
    val internalMetadata = fooInternal.kotlinMetadata
    internalMetadata.shouldNotBeNull()

    internalMetadata.kmClass.name.toQualifiedName() shouldBe fooInternal.qualifiedName
    internalMetadata.kmClass.supertypes shouldHaveSize 1
    internalMetadata.kmClass.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Any"
    }

    internalMetadata.kmClass.constructors shouldHaveSize 1
    internalMetadata.kmClass.functions shouldHaveSize 1
    internalMetadata.kmClass.functions.map { it.name } shouldContainExactly listOf("printInternal")
  }

  @Test
  fun `metadata changes are saved to new jar`() {
    val oldJar = ktLibraryPath.loadJar()
    val newJar: GenericJarArchive

    withClasspath(oldJar) { cp ->
      val metadata = cp["sh.christian.mylibrary.Foo"].kotlinMetadata
      metadata.shouldNotBeNull()
      metadata.kmClass.functions.removeIf { it.name == "printInternal" }

      val internalMetadata = cp["sh.christian.mylibrary.FooInternal"].kotlinMetadata
      internalMetadata.shouldNotBeNull()
      internalMetadata.kmClass.functions.single().name = "print"

      newJar = cp.toGenericJarArchive()
    }

    withClasspath(newJar) { cp ->
      val metadata = cp["sh.christian.mylibrary.Foo"].kotlinMetadata
      metadata.shouldNotBeNull()
      metadata.kmClass.functions shouldHaveSize 1
      metadata.kmClass.functions.map { it.name } shouldContainExactly listOf("print")

      val internalMetadata = cp["sh.christian.mylibrary.FooInternal"].kotlinMetadata
      internalMetadata.shouldNotBeNull()
      internalMetadata.kmClass.functions shouldHaveSize 1
      internalMetadata.kmClass.functions.map { it.name } shouldContainExactly listOf("print")
    }
  }
}
