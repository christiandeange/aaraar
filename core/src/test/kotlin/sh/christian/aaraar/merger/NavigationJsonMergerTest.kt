package sh.christian.aaraar.merger

import io.kotest.matchers.collections.shouldContainExactly
import sh.christian.aaraar.model.NavigationJson
import sh.christian.aaraar.utils.navigationJsonData
import sh.christian.aaraar.utils.navigationJsonDataString
import sh.christian.aaraar.utils.withFile
import sh.christian.aaraar.utils.withFileSystem
import java.nio.file.Files
import kotlin.test.Test

class NavigationJsonMergerTest {

  private val merger = NavigationJsonMerger()

  @Test
  fun `parses navigation data json`() = withFile {
    Files.writeString(filePath, navigationJsonDataString("nav1", "/lib1"))

    val navigationJson = NavigationJson.from(filePath)
    navigationJson.navigationData shouldContainExactly listOf(navigationJsonData("nav1", "/lib1"))
  }

  @Test
  fun `simple merge with two navigation graphs`() = withFileSystem {
    val json1 = NavigationJson.from(withFile { Files.writeString(filePath, navigationJsonDataString("nav1", "/lib1")) })
    val json2 = NavigationJson.from(withFile { Files.writeString(filePath, navigationJsonDataString("nav2", "/lib2")) })

    val merged = merger.merge(json1, json2)

    merged.navigationData shouldContainExactly listOf(
      navigationJsonData("nav1", "/lib1"),
      navigationJsonData("nav2", "/lib2")
    )
  }

  @Test
  fun `merge with two of same name keeps both`() = withFileSystem {
    val json1 = NavigationJson.from(withFile { Files.writeString(filePath, navigationJsonDataString("nav", "/lib1")) })
    val json2 = NavigationJson.from(withFile { Files.writeString(filePath, navigationJsonDataString("nav", "/lib2")) })

    val merged = merger.merge(json1, json2)

    merged.navigationData shouldContainExactly listOf(
      navigationJsonData("nav", "/lib1"),
      navigationJsonData("nav", "/lib2"),
    )
  }
}
