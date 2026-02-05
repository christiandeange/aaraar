package sh.christian.aaraar.utils

import com.android.ide.common.blame.SourceFile
import com.android.ide.common.blame.SourceFilePosition
import com.android.ide.common.blame.SourcePosition
import com.android.manifmerger.DeepLink
import com.android.manifmerger.NavigationXmlDocumentData
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.primaryConstructor

private object ResourceLoader

private val resourceLoader = ResourceLoader::class.java.classLoader

val annotationsJarPath: Path by testFixtureJar()
val animalJarPath: Path by testFixtureJar()
val fooJarPath: Path by testFixtureJar()
val foo2JarPath: Path by testFixtureJar()
val ktLibraryJarPath: Path by testFixtureJar()
val serviceJarPath: Path by testFixtureJar()

val externalLibsPath: Path
  get() = Paths.get(resourceLoader.getResource("libs")!!.toURI())

private val root: File
  get() = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }.last()

val agpVersion: Version
  get() = Version.parse(com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION)

val deepLinkActionVersion = Version.parse("8.0.0-dev")
val deepLinkFragmentVersion = Version.parse("8.2.0-alpha07")

fun navigationJsonData(
  name: String,
  path: String,
): NavigationXmlDocumentData {
  val deepLinkConstructorArguments = buildMap {
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

    if (agpVersion >= deepLinkActionVersion) {
      put("action", "android.intent.action.VIEW")
    }
    if (agpVersion >= deepLinkFragmentVersion) {
      put("fragment", null)
    }
  }

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

fun navigationJsonDataString(
  name: String,
  path: String
): String {
  val action = if (agpVersion < deepLinkActionVersion) {
    ""
  } else {
    """,
        "action": "android.intent.action.VIEW""""
  }

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
        "isAutoVerify": false$action
      }
    ]
  }
]
""".trimIndent()
}

private fun testFixtureJar(): ReadOnlyProperty<Any?, Path> {
  return ReadOnlyProperty { _, property ->
    Paths.get(resourceLoader.getResource("${property.name.removeSuffix("JarPath")}.jar")!!.toURI())
  }
}