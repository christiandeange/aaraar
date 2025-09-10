package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.ktLibraryJarPath
import kotlin.test.Test

class JniTest {
  @Test
  fun `test equality`() {
    val jni1 = Jni.from(ktLibraryJarPath.parent)
    val jni2 = Jni.from(ktLibraryJarPath.parent)
    jni1 shouldBe jni2
  }
}
