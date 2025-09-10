package sh.christian.aaraar.model

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.serviceJarPath
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

  @Test
  fun `test equality with meta files`() {
    val jar1 = GenericJarArchive.from(serviceJarPath, keepMetaFiles = true)
    val jar2 = GenericJarArchive.from(serviceJarPath, keepMetaFiles = true)
    jar1 shouldBe jar2
  }

  @Test
  fun `test equality without meta files`() {
    val jar1 = GenericJarArchive.from(serviceJarPath, keepMetaFiles = false)
    val jar2 = GenericJarArchive.from(serviceJarPath, keepMetaFiles = false)
    jar1 shouldBe jar2
  }

  @Test
  fun `test equality with differing meta files`() {
    val jar1 = GenericJarArchive.from(serviceJarPath, keepMetaFiles = true)
    val jar2 = GenericJarArchive.from(serviceJarPath, keepMetaFiles = false)
    jar1 shouldNotBe jar2
  }
}
