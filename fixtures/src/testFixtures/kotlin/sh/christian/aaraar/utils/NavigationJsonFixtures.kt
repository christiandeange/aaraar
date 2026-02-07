package sh.christian.aaraar.utils

import com.android.ide.common.blame.SourceFile
import com.android.ide.common.blame.SourceFilePosition
import com.android.ide.common.blame.SourcePosition
import com.android.manifmerger.DeepLink
import com.android.manifmerger.NavigationXmlDocumentData
import java.io.File
import kotlin.reflect.full.primaryConstructor

abstract class NavigationJsonFixtures(
  protected val name: String,
  protected val path: String,
) {
  abstract fun asData(): NavigationXmlDocumentData

  abstract fun asString(): String

  companion object {
    fun get(
      name: String,
      path: String,
    ): NavigationJsonFixtures {
      val agpVersion = Version.parse(com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION)

      return when {
        agpVersion >= Version.parse("8.2.0-alpha07") -> NavigationJsonFixturesWithFragment(name, path)
        agpVersion >= Version.parse("8.1.0-alpha01") -> NavigationJsonFixturesWithAction(name, path)
        else -> BaseNavigationJsonFixtures(name, path)
      }
    }
  }
}

private open class BaseNavigationJsonFixtures(
  name: String,
  path: String,
) : NavigationJsonFixtures(name, path) {
  protected open fun provideDataConstructorParameters(): Map<String, Any?> {
    return buildMap {
      put("schemes", listOf("http", "https"))
      put("host", "www.example.com")
      put("port", -1)
      put("path", path)
      put("query", null)
      put(
        "sourceFilePosition",
        SourceFilePosition(
          SourceFile(root.resolve("$name.xml"), name),
          SourcePosition(7, 4, 309, 9, 37, 440),
        )
      )
      put("isAutoVerify", false)
    }
  }

  override fun asData(): NavigationXmlDocumentData {
    val deepLinkConstructorArguments = provideDataConstructorParameters()

    val deepLink = DeepLink::class.primaryConstructor!!.callBy(
      deepLinkConstructorArguments.mapKeys { arg ->
        DeepLink::class.primaryConstructor!!.parameters.first { it.name == arg.key }
      }
    )

    return NavigationXmlDocumentData(
      name = name,
      navigationXmlIds = emptyList(),
      deepLinks = listOf(deepLink),
    )
  }

  override fun asString(): String {
    return """
      [
        {
          "name": "$name",
          "navigationXmlIds": [],
          "deepLinks": [
            {
              "schemes": [
                "http",
                "https"
              ],
              "host": "www.example.com",
              "port": -1,
              "path": "$path",
              "sourceFilePosition": {
                "mSourceFile": {
                  "mFilePath": "${root.resolve("$name.xml").toString().replace("\\", "\\\\")}",
                  "mDescription": "$name"
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
              "isAutoVerify": false
            }
          ]
        }
      ]
      """.trimIndent()
  }
}

private open class NavigationJsonFixturesWithAction(
  name: String,
  path: String,
) : BaseNavigationJsonFixtures(name, path) {
  override fun provideDataConstructorParameters(): Map<String, Any?> {
    return super.provideDataConstructorParameters().plus("action" to "android.intent.action.VIEW")
  }

  override fun asString(): String {
    return """
      [
        {
          "name": "$name",
          "navigationXmlIds": [],
          "deepLinks": [
            {
              "schemes": [
                "http",
                "https"
              ],
              "host": "www.example.com",
              "port": -1,
              "path": "$path",
              "sourceFilePosition": {
                "mSourceFile": {
                  "mFilePath": "${root.resolve("$name.xml").toString().replace("\\", "\\\\")}",
                  "mDescription": "$name"
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
}

private open class NavigationJsonFixturesWithFragment(
  name: String,
  path: String,
) : NavigationJsonFixturesWithAction(name, path) {
  override fun provideDataConstructorParameters(): Map<String, Any?> {
    return super.provideDataConstructorParameters().plus("fragment" to null)
  }
}

private val root: File
  get() = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }.last()
