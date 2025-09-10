package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import sh.christian.aaraar.utils.serviceJarPath
import kotlin.test.Test

class ClassesTest {
  @Test
  fun `test equality with meta files`() {
    val classes1 = Classes.from(serviceJarPath, keepMetaFiles = true)
    val classes2 = Classes.from(serviceJarPath, keepMetaFiles = true)
    classes1 shouldBe classes2
  }

  @Test
  fun `test equality without meta files`() {
    val classes1 = Classes.from(serviceJarPath, keepMetaFiles = false)
    val classes2 = Classes.from(serviceJarPath, keepMetaFiles = false)
    classes1 shouldBe classes2
  }

  @Test
  fun `test equality with differing meta files`() {
    val classes1 = Classes.from(serviceJarPath, keepMetaFiles = true)
    val classes2 = Classes.from(serviceJarPath, keepMetaFiles = false)
    classes1 shouldNotBe classes2
  }
}
