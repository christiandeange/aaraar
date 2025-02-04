package sh.christian.aaraar.model.classeditor.metadata

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.classeditor.NewParameter
import sh.christian.aaraar.model.classeditor.name
import sh.christian.aaraar.model.classeditor.requireMetadata
import sh.christian.aaraar.model.classeditor.types.intType
import sh.christian.aaraar.model.classeditor.types.objectType
import sh.christian.aaraar.model.classeditor.types.stringType
import sh.christian.aaraar.utils.ktLibraryPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class ConstructorMetadataTest {

  @Test
  fun `remove constructor`() = withClasspath(ktLibraryPath.loadJar()) { cp ->
    cp.name.constructors shouldHaveSize 1
    cp.name.requireMetadata().constructors shouldHaveSize 1

    cp.name.constructors = emptyList()
    cp.name.finalizeClass()

    cp.name.constructors.shouldBeEmpty()
    cp.name.requireMetadata().constructors.shouldBeEmpty()
  }

  @Test
  fun `change constructor parameter`() = withClasspath(ktLibraryPath.loadJar()) { cp ->
    cp.name.requireMetadata().constructors.single().valueParameters.single().let { parameterMetadata ->
      parameterMetadata.name shouldBe "name"
      parameterMetadata.type.classifier shouldBe cp.kmClassifier("kotlin.String")
    }
    cp.name.constructors.single().parameters.single().let { parameter ->
      parameter.name shouldBe "name"
      parameter.type shouldBe cp.stringType

      parameter.name = "myName"
      parameter.type = cp.objectType
    }
    cp.name.finalizeClass()

    cp.name.constructors.single().parameters.single().let { parameter ->
      parameter.name shouldBe "myName"
      parameter.type shouldBe cp.objectType
    }
    cp.name.requireMetadata().constructors.single().valueParameters.single().let { parameterMetadata ->
      parameterMetadata.name shouldBe "myName"
      parameterMetadata.type.classifier shouldBe cp.kmClassifier(cp.objectType.qualifiedName)
    }
  }

  @Test
  fun `set constructor parameters`() = withClasspath(ktLibraryPath.loadJar()) { cp ->
    cp.name.constructors.single().parameters shouldHaveSize 1
    cp.name.requireMetadata().constructors.single().valueParameters shouldHaveSize 1

    cp.name.constructors.single().setParameters(
      NewParameter("myName", cp.stringType),
      NewParameter("myAge", cp.intType),
    )
    cp.name.finalizeClass()

    val (param0, param1) = cp.name.constructors.single().parameters
    param0.name shouldBe "myName"
    param0.type shouldBe cp.stringType
    param1.name shouldBe "myAge"
    param1.type shouldBe cp.intType

    val (metadataParam0, metadataParam1) = cp.name.requireMetadata().constructors.single().valueParameters
    metadataParam0.name shouldBe "myName"
    metadataParam0.type.classifier shouldBe cp.kmClassifier("kotlin.String")
    metadataParam1.name shouldBe "myAge"
    metadataParam1.type.classifier shouldBe cp.kmClassifier("kotlin.Int")
  }
}
