package sh.christian.aaraar.utils

import com.android.ide.common.blame.SourceFile
import com.android.ide.common.blame.SourceFilePosition
import com.android.ide.common.blame.SourcePosition
import com.android.manifmerger.DeepLink
import com.android.manifmerger.NavigationXmlDocumentData
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

private object ResourceLoader

private val resourceLoader = ResourceLoader::class.java.classLoader

val annotationsJarPath: Path
  get() = Paths.get(resourceLoader.getResource("annotations.jar")!!.toURI())

val animalJarPath: Path
  get() = Paths.get(resourceLoader.getResource("animal.jar")!!.toURI())

val fooJarPath: Path
  get() = Paths.get(resourceLoader.getResource("foo.jar")!!.toURI())

val foo2JarPath: Path
  get() = Paths.get(resourceLoader.getResource("foo2.jar")!!.toURI())

val externalLibsPath: Path
  get() = Paths.get(resourceLoader.getResource("libs")!!.toURI())

val ktLibraryPath: Path
  get() = Paths.get(resourceLoader.getResource("ktLibrary.jar")!!.toURI())

private val root: File
  get() = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }.last()

fun navigationJsonData(
  name: String,
  path: String,
): NavigationXmlDocumentData = NavigationXmlDocumentData(
  name = name,
  navigationXmlIds = emptyList(),
  deepLinks = listOf(
    DeepLink(
      schemes = listOf("http", "https"),
      host = "www.example.com",
      port = -1,
      path = path,
      query = null,
      fragment = null,
      sourceFilePosition = SourceFilePosition(
        SourceFile(root.resolve("$name.xml"), name),
        SourcePosition(7, 4, 309, 9, 37, 440),
      ),
      isAutoVerify = false,
    )
  ),
)

fun navigationJsonDataString(
  name: String,
  path: String
): String = """
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
