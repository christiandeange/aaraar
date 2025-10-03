package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.navigationJsonDataString
import java.io.File
import kotlin.test.Test

class NavigationJsonTest {
  @Test
  fun `test toString`() {
    val json = navigationJsonDataString("nav1", "/lib1")
    val navigationJson = NavigationJson(json)

    val filePath = if (File.separatorChar == '\\') {
      """D:\\nav1.xml"""
    } else {
      "/nav1.xml"
    }

    navigationJson.toString() shouldBe """
      [
        {
          "name": "nav1",
          "navigationXmlIds": [],
          "deepLinks": [
            {
              "schemes": [
                "http",
                "https"
              ],
              "host": "www.example.com",
              "port": -1,
              "path": "/lib1",
              "sourceFilePosition": {
                "mSourceFile": {
                  "mFilePath": "$filePath",
                  "mDescription": "nav1"
                },
                "mSourcePosition": {
                  "mStartLine": 7,
                  "mStartColumn": 4,
                  "mStartOffset": 309,
                  "mEndLine": 9,
                  "mEndColumn": 37,
                  "mEndOffset": 440
                }
              },
              "isAutoVerify": false,
              "action": "android.intent.action.VIEW"
            }
          ]
        }
      ]
    """.trimIndent()
  }

  @Test
  fun `test equality`() {
    val json = navigationJsonDataString("nav1", "/lib1")

    val navigationJson1 = NavigationJson(json)
    val navigationJson2 = NavigationJson(json)
    navigationJson1 shouldBe navigationJson2
  }
}
