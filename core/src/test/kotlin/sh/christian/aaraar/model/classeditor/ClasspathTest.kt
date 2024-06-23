package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.maps.shouldNotHaveKey
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.utils.jetbrainsAnnotationsJarPath
import sh.christian.aaraar.utils.loadJar
import kotlin.test.Test

class ClasspathTest {
  @Test
  fun `ignores new enums`() {
    val jar: GenericJarArchive

    withClasspath { cp ->
      cp.addClass("com.example.MyClass")
      cp.addClass("com.example.MyEnum") {
        modifiers += Modifier.ENUM
        superclass = cp["java.lang.Enum"]
      }
      jar = cp.toGenericJarArchive()
    }

    jar shouldHaveSize 1
    jar shouldHaveKey "com/example/MyClass.class"
    jar shouldNotHaveKey "com/example/MyEnum.class"
  }

  @Test
  fun `ignores changes to existing enums`() {
    val oldJar = jetbrainsAnnotationsJarPath.loadJar()
    val newJar: GenericJarArchive

    withClasspath(oldJar) { cp ->
      val capitalizationClass = cp["org.jetbrains.annotations.Nls${'$'}Capitalization"]
      capitalizationClass.superclass?.qualifiedName shouldBe "java.lang.Enum"
      capitalizationClass.interfaces.shouldBeEmpty()

      capitalizationClass.interfaces += cp["java.io.Serializable"]
      newJar = cp.toGenericJarArchive()
    }

    withClasspath(newJar) { cp ->
      val capitalizationClass = cp["org.jetbrains.annotations.Nls${'$'}Capitalization"]

      capitalizationClass.superclass?.qualifiedName shouldBe "java.lang.Enum"
      capitalizationClass.interfaces.shouldBeEmpty()
    }
  }
}
