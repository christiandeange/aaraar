package sh.christian.aaraar.model.lib

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.nativelibJarPath
import java.nio.file.Paths
import kotlin.io.path.writeBytes
import kotlin.test.Test

class NativeLibraryTest {

  @Test
  fun `reading and writing simple native library`() {
    val inBytes = nativelibJarPath.loadJar()["libsentry-android.so"]!!
    val lib = NativeLibrary.from(inBytes)
    val outBytes = lib.bytes()

    Paths.get("/Users/chr/Desktop/libsentry-android-new.so").writeBytes(outBytes)
    inBytes.asList() shouldBe outBytes.asList()
  }
}
