package sh.christian.aaraar.model.classeditor.metadata

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmFunction
import sh.christian.aaraar.model.classeditor.NewParameter
import sh.christian.aaraar.model.classeditor.name
import sh.christian.aaraar.model.classeditor.requireMetadata
import sh.christian.aaraar.model.classeditor.types.intType
import sh.christian.aaraar.model.classeditor.types.objectType
import sh.christian.aaraar.model.classeditor.types.stringType
import sh.christian.aaraar.model.classeditor.types.voidType
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class MethodMetadataTest {

  @Test
  fun `remove method`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.methods.map { it.name }
      .shouldContainExactlyInAnyOrder("printName", "updateName", "getName", "setName")
    cp.name.requireMetadata().functions.map { it.name }
      .shouldContainExactlyInAnyOrder("printName", "updateName")

    cp.name.methods = cp.name.methods.filter { it.name != "updateName" }
    cp.name.finalizeClass()

    cp.name.methods.map { it.name }
      .shouldContainExactlyInAnyOrder("printName", "getName", "setName")
    cp.name.requireMetadata().functions.map { it.name }
      .shouldContainExactlyInAnyOrder("printName")
  }

  @Test
  fun `set method name`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.getMethod("printName").shouldNotBeNull()
    cp.name.requireMetadata().functions.map { it.name }
      .shouldContainExactlyInAnyOrder("printName", "updateName")

    cp.name.getMethod("printName")!!.name = "printNameInternal"
    cp.name.finalizeClass()

    cp.name.getMethod("printName").shouldBeNull()
    cp.name.getMethod("printNameInternal").shouldNotBeNull()
    cp.name.requireMetadata().functions.map { it.name }
      .shouldContainExactlyInAnyOrder("printNameInternal", "updateName")
  }

  @Test
  fun `set method return type`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.getMethod("printName")!!.returnType shouldBe cp.voidType
    cp.name.requireMetadata().printNameFunction.returnType.classifier shouldBe cp.kmClassifier("kotlin.Unit")

    cp.name.getMethod("printName")!!.returnType = cp.stringType
    cp.name.finalizeClass()

    cp.name.getMethod("printName")!!.returnType shouldBe cp.stringType
    cp.name.requireMetadata().printNameFunction.returnType.classifier shouldBe cp.kmClassifier("kotlin.String")
  }

  @Test
  fun `change method parameter`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.getMethod("updateName")!!.parameters.single().let { parameter ->
      parameter.name shouldBe "newName"
      parameter.type shouldBe cp.stringType
      cp.name.requireMetadata().updateNameFunction.valueParameters.single().let { parameterMetadata ->
        parameterMetadata.name shouldBe "newName"
        parameterMetadata.type.classifier shouldBe cp.kmClassifier("kotlin.String")
      }

      parameter.name = "myName"
      parameter.type = cp.objectType
    }
    cp.name.finalizeClass()

    cp.name.getMethod("updateName")!!.parameters.single().let { parameter ->
      parameter.name shouldBe "myName"
      parameter.type shouldBe cp.objectType

      cp.name.requireMetadata().updateNameFunction.valueParameters.single().let { parameterMetadata ->
        parameterMetadata.name shouldBe "myName"
        parameterMetadata.type.classifier shouldBe cp.kmClassifier(cp.objectType.qualifiedName)
      }
    }
  }

  @Test
  fun `set method parameters`() = withClasspath(ktLibraryJarPath.loadJar()) { cp ->
    cp.name.getMethod("updateName")!!.parameters shouldHaveSize 1
    cp.name.requireMetadata().updateNameFunction.valueParameters shouldHaveSize 1

    cp.name.getMethod("updateName")!!.setParameters(
      NewParameter("myName", cp.stringType),
      NewParameter("myAge", cp.intType),
    )
    cp.name.finalizeClass()

    val (param0, param1) = cp.name.getMethod("updateName")!!.parameters
    param0.name shouldBe "myName"
    param0.type shouldBe cp.stringType
    param1.name shouldBe "myAge"
    param1.type shouldBe cp.intType

    val (metadataParam0, metadataParam1) = cp.name.requireMetadata().updateNameFunction.valueParameters
    metadataParam0.name shouldBe "myName"
    metadataParam0.type.classifier shouldBe cp.kmClassifier("kotlin.String")
    metadataParam1.name shouldBe "myAge"
    metadataParam1.type.classifier shouldBe cp.kmClassifier("kotlin.Int")
  }

  private val KmClass.updateNameFunction: KmFunction
    get() = functions.single { it.name == "updateName" }

  private val KmClass.printNameFunction: KmFunction
    get() = functions.single { it.name == "printName" }
}
