package sh.christian.aaraar.model.classeditor.metadata

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.classeditor.name
import sh.christian.aaraar.model.classeditor.requireMetadata
import sh.christian.aaraar.model.classeditor.types.objectType
import sh.christian.aaraar.model.classeditor.types.stringType
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class FieldMetadataTest {

  @Test
  fun `remove field`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.fields.map { it.name }.shouldContainExactlyInAnyOrder("name")
    cp.name.requireMetadata().properties.map { it.name }.shouldContainExactlyInAnyOrder("name")

    cp.name.fields = emptyList()
    cp.name.methods = cp.name.methods.filter { it.name != "getName" && it.name != "setName" }
    cp.name.finalizeClass()

    cp.name.requireMetadata().properties.shouldBeEmpty()
  }

  @Test
  fun `set field name`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.getField("name").shouldNotBeNull()
    cp.name.requireMetadata().properties.map { it.name }.shouldContainExactlyInAnyOrder("name")

    cp.name.getField("name")!!.name = "myName"
    cp.name.finalizeClass()

    cp.name.getField("name").shouldBeNull()
    cp.name.getField("myName").shouldNotBeNull()
    cp.name.requireMetadata().properties.map { it.name }.shouldContainExactlyInAnyOrder("myName")
  }

  @Test
  fun `set field type`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.fields.single().type shouldBe cp.stringType
    cp.name.requireMetadata().properties.single().returnType.classifier shouldBe cp.kmClassifier("kotlin.String")

    cp.name.fields.single().type = cp.objectType
    cp.name.finalizeClass()

    cp.name.fields.single().type shouldBe cp.objectType
    cp.name.requireMetadata().properties.single().returnType.classifier shouldBe cp.kmClassifier("kotlin.Any")
  }
}
