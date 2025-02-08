package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.utils.annotationsJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shouldContainExactly
import sh.christian.aaraar.utils.withClasspath
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

    jar.shouldContainExactly("com/example/MyClass.class")
  }

  @Test
  fun `ignores changes to existing enums`() {
    val oldJar = annotationsJarPath.loadJar()
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
