package sh.christian.aaraar.model

import io.kotest.matchers.maps.shouldContainExactly
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.withFileSystem
import java.nio.file.Files
import kotlin.test.Test

class PrefabTest {

  @Test
  fun `reads and writes Prefab package files`() {
    withFileSystem {
      val prefabPath = Files.createDirectories(root / "prefab")
      Files.writeString(prefabPath / "prefab.json", "{\"name\":\"example\"}")
      val libraryPath = Files.createDirectories(prefabPath / "modules" / "example" / "libs" / "android.x86")
      Files.writeString(libraryPath / "abi.json", "{\"abi\":\"x86\"}")
      Files.write(libraryPath / "libexample.so", byteArrayOf(1, 2, 3))

      val prefab = Prefab.from(prefabPath)

      withDirectory {
        prefab.writeTo(root)
        files().shouldContainExactly(
          mapOf(
            "prefab.json" to "{\"name\":\"example\"}",
            "modules/example/libs/android.x86/abi.json" to "{\"abi\":\"x86\"}",
            "modules/example/libs/android.x86/libexample.so" to "\u0001\u0002\u0003",
          ),
        )
      }
    }
  }
}
