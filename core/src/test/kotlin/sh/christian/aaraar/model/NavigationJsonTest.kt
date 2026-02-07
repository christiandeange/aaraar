package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.NavigationJsonFixtures
import kotlin.test.Test

class NavigationJsonTest {
  @Test
  fun `test toString`() {
    val json = NavigationJsonFixtures.get("nav1", "/lib1").asString()

    val navigationJson = NavigationJson(json)
    navigationJson.toString() shouldBe json
  }

  @Test
  fun `test equality`() {
    val json = NavigationJsonFixtures.get("nav1", "/lib1").asString()

    val navigationJson1 = NavigationJson(json)
    val navigationJson2 = NavigationJson(json)
    navigationJson1 shouldBe navigationJson2
  }
}
