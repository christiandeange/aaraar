package sh.christian.aaraar.shading

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath

class GenericJarArchiveNonStandardTest {
  @Test
  fun `no shading`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses shouldHaveKey "sh/christian/mylibrary/Name-Maker.class"
    originalClasses shouldHaveKey "sh/christian/mylibrary/Name-Maker${'$'}Companion.class"
  }

  @Test
  fun `shade by class name with classes with non-java characters`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
      classRenames = mapOf("sh.christian.mylibrary.Name-Maker" to "sh.christian.mylibrary.NameMaker"),
    )
    shadedClasses shouldHaveKey "sh/christian/mylibrary/NameMaker.class"
    shadedClasses shouldHaveKey "sh/christian/mylibrary/Name-Maker${'$'}Companion.class"
  }

  @Test
  fun `shade by class name with inner classes with classes with non-java characters`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
      classRenames = mapOf("sh.christian.mylibrary.Name-Maker**" to "sh.christian.mylibrary.NameMaker@1"),
    )
    shadedClasses shouldHaveKey "sh/christian/mylibrary/NameMaker.class"
    shadedClasses shouldHaveKey "sh/christian/mylibrary/NameMaker${'$'}Companion.class"
  }

  @Test
  fun `shade by package name with classes with non-java characters`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
      classRenames = mapOf("sh.christian.mylibrary.**" to "sh.christian.lib.@1"),
    )
    shadedClasses shouldHaveKey "sh/christian/lib/Name-Maker.class"
    shadedClasses shouldHaveKey "sh/christian/lib/Name-Maker${'$'}Companion.class"
  }

  @Test
  fun `shade by package name with arrays`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
      classRenames = mapOf("sh.christian.mylibrary.**" to "sh.christian.lib.@1"),
    )

    withClasspath(shadedClasses) { cp ->
      val fields = cp["sh.christian.lib.Foos"].fields

      fields shouldHaveSize 1
      fields[0].name shouldBe "twoFoos"
      fields[0].type.qualifiedName shouldBe "sh.christian.lib.Foo[]"
    }
  }
}
