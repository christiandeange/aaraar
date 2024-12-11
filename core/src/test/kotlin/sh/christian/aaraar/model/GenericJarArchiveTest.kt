package sh.christian.aaraar.model

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withFile
import kotlin.test.Test

class GenericJarArchiveTest {

  @Test
  fun `can read from written jar`() {
    val originalJar = animalJarPath.loadJar()

    withFile {
      filePath.deleteIfExists()
      originalJar.writeTo(filePath)

      val rehydratedJar = filePath.loadJar()

      rehydratedJar.keys shouldContainExactly originalJar.keys
      rehydratedJar.keys.forEach { key ->
        withClue("Jar entry: $key") {
          rehydratedJar[key]!! shouldBe originalJar[key]!!
        }
      }
    }
  }

  @Test
  fun `empty jar has empty bytes`() {
    GenericJarArchive.NONE.bytes() shouldBe byteArrayOf()
  }
}
