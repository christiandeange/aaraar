package sh.christian.aaraar.model.classeditor.metadata

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.metadata.Visibility
import kotlinx.metadata.visibility
import sh.christian.aaraar.model.classeditor.Modifier
import sh.christian.aaraar.model.classeditor.foo
import sh.christian.aaraar.model.classeditor.requireMetadata
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class ClassMetadataTest {

  @Test
  fun `change class qualified name`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    val foo = cp["sh.christian.mylibrary.Foo"]

    foo.qualifiedName shouldBe "sh.christian.mylibrary.Foo"
    foo.requireMetadata().name shouldBe "sh/christian/mylibrary/Foo"

    foo.qualifiedName = "sh.christian.external.TheFoo"
    foo.finalizeClass()

    foo.qualifiedName shouldBe "sh.christian.external.TheFoo"
    foo.requireMetadata().name shouldBe "sh/christian/external/TheFoo"
  }

  @Test
  fun `change class simple name`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    val foo = cp["sh.christian.mylibrary.Foo"]

    foo.qualifiedName shouldBe "sh.christian.mylibrary.Foo"
    foo.requireMetadata().name shouldBe "sh/christian/mylibrary/Foo"

    foo.simpleName = "TheFoo"
    foo.finalizeClass()

    foo.qualifiedName shouldBe "sh.christian.mylibrary.TheFoo"
    foo.requireMetadata().name shouldBe "sh/christian/mylibrary/TheFoo"
  }

  @Test
  fun `change class package name`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    val foo = cp["sh.christian.mylibrary.Foo"]

    foo.qualifiedName shouldBe "sh.christian.mylibrary.Foo"
    foo.requireMetadata().name shouldBe "sh/christian/mylibrary/Foo"

    foo.packageName = "sh.christian.external"
    foo.finalizeClass()

    foo.qualifiedName shouldBe "sh.christian.external.Foo"
    foo.requireMetadata().name shouldBe "sh/christian/external/Foo"
  }

  @Test
  fun `change visibility`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.foo.modifiers shouldBe setOf(Modifier.PUBLIC, Modifier.FINAL)
    cp.foo.requireMetadata().visibility shouldBe Visibility.PUBLIC

    cp.foo.modifiers = setOf(Modifier.PRIVATE, Modifier.FINAL)
    cp.foo.finalizeClass()

    cp.foo.modifiers shouldBe setOf(Modifier.PRIVATE, Modifier.FINAL)
    cp.foo.requireMetadata().visibility shouldBe Visibility.PRIVATE
  }

  @Test
  fun `change superclass`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.foo.superclass.shouldBeNull()
    cp.foo.requireMetadata().supertypes.map { it.classifier }
      .shouldContainExactly(cp.kmClassifier("kotlin.Any"))

    cp.foo.superclass = cp["java.io.Reader"]
    cp.foo.finalizeClass()

    cp.foo.superclass shouldBe cp["java.io.Reader"]
    cp.foo.requireMetadata().supertypes.map { it.classifier }
      .shouldContainExactly(cp.kmClassifier("java.io.Reader"))
  }

  @Test
  fun `add interface`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.foo.interfaces.shouldBeEmpty()
    cp.foo.requireMetadata().supertypes.map { it.classifier }
      .shouldContainExactly(cp.kmClassifier("kotlin.Any"))

    cp.foo.interfaces = listOf(cp["java.io.Serializable"])
    cp.foo.finalizeClass()

    cp.foo.interfaces shouldContainExactly listOf(cp["java.io.Serializable"])
    cp.foo.requireMetadata().supertypes.map { it.classifier }
      .shouldContainExactly(cp.kmClassifier("kotlin.Any"), cp.kmClassifier("java.io.Serializable"))
  }
}
