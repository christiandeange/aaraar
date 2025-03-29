package sh.christian.aaraar.model.lib

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.nativelibJarPath
import kotlin.test.Test

class NativeLibraryTest {

  @Test
  fun `reading and writing simple native library`() {
    val inBytes = nativelibJarPath.loadJar()["libsentry-android.so"]!!
    val lib = NativeLibrary.from(inBytes)

    val outBytes = lib.bytes()
    inBytes.asList() shouldBe outBytes.asList()
  }
}
