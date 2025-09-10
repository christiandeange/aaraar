package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import sh.christian.aaraar.utils.serviceJarPath
import kotlin.test.Test

class ApiJarTest {
  @Test
  fun `test equality with meta files`() {
    val apiJar1 = ApiJar.from(serviceJarPath, keepMetaFiles = true)
    val apiJar2 = ApiJar.from(serviceJarPath, keepMetaFiles = true)
    apiJar1 shouldBe apiJar2
  }

  @Test
  fun `test equality without meta files`() {
    val apiJar1 = ApiJar.from(serviceJarPath, keepMetaFiles = false)
    val apiJar2 = ApiJar.from(serviceJarPath, keepMetaFiles = false)
    apiJar1 shouldBe apiJar2
  }

  @Test
  fun `test equality with differing meta files`() {
    val apiJar1 = ApiJar.from(serviceJarPath, keepMetaFiles = true)
    val apiJar2 = ApiJar.from(serviceJarPath, keepMetaFiles = false)
    apiJar1 shouldNotBe apiJar2
  }
}
