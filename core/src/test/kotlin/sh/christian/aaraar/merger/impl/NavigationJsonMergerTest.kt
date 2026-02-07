package sh.christian.aaraar.merger.impl

import io.kotest.matchers.collections.shouldContainExactly
import sh.christian.aaraar.model.NavigationJson
import sh.christian.aaraar.utils.NavigationJsonFixtures
import sh.christian.aaraar.utils.withFile
import sh.christian.aaraar.utils.withFileSystem
import java.nio.file.Files
import kotlin.test.Test

class NavigationJsonMergerTest {

  private val merger = NavigationJsonMerger()

  @Test
  fun `parses navigation data json`() = withFile {
    val nav = NavigationJsonFixtures.get("nav1", "/lib1")
    Files.writeString(filePath, nav.asString())

    val navigationJson = NavigationJson.from(filePath)
    navigationJson.navigationData shouldContainExactly listOf(nav.asData())
  }

  @Test
  fun `simple merge with two navigation graphs`() = withFileSystem {
    val nav1 = NavigationJsonFixtures.get("nav1", "/lib1")
    val nav2 = NavigationJsonFixtures.get("nav2", "/lib2")

    val json1 = NavigationJson.from(withFile { Files.writeString(filePath, nav1.asString()) })
    val json2 = NavigationJson.from(withFile { Files.writeString(filePath, nav2.asString()) })

    val merged = merger.merge(json1, json2)

    merged.navigationData shouldContainExactly listOf(nav1.asData(), nav2.asData())
  }

  @Test
  fun `merge with two of same name keeps both`() = withFileSystem {
    val nav1 = NavigationJsonFixtures.get("nav", "/lib1")
    val nav2 = NavigationJsonFixtures.get("nav", "/lib2")

    val json1 = NavigationJson.from(withFile { Files.writeString(filePath, nav1.asString()) })
    val json2 = NavigationJson.from(withFile { Files.writeString(filePath, nav2.asString()) })

    val merged = merger.merge(json1, json2)

    merged.navigationData shouldContainExactly listOf(nav1.asData(), nav2.asData())
  }
}
