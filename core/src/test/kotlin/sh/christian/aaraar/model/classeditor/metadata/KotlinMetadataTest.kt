package sh.christian.aaraar.model.classeditor.metadata

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.metadata.KmClassifier
import sh.christian.aaraar.model.classeditor.foo
import sh.christian.aaraar.model.classeditor.fooInternal
import sh.christian.aaraar.model.classeditor.requireMetadata
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
    val metadata = cp.foo.requireMetadata()

    metadata.name.toQualifiedName() shouldBe cp.foo.qualifiedName
    metadata.supertypes shouldHaveSize 1
    metadata.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Any"
    }

    metadata.constructors shouldHaveSize 1
    metadata.functions.map { it.name }.shouldContainExactlyInAnyOrder("print", "printInternal")

    val internalMetadata = cp.fooInternal.requireMetadata()

    internalMetadata.name.toQualifiedName() shouldBe cp.fooInternal.qualifiedName
    internalMetadata.supertypes shouldHaveSize 1
    internalMetadata.supertypes.first().classifier.should { classifier ->
      classifier.shouldBeInstanceOf<KmClassifier.Class>()
      classifier.name.toQualifiedName() shouldBe "kotlin.Any"
    }

    internalMetadata.constructors shouldHaveSize 1
    internalMetadata.functions.map { it.name } shouldContainExactly listOf("printInternal")
  }
}
