package sh.christian.aaraar.model

import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.parse
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the contents of the `AndroidManifest.xml` file.
 */
class AndroidManifest
internal constructor(
  private val manifestNode: Node,
) {
  val packageName: String by lazy {
    manifestNode.get<String>("package")!!
  }

  val minSdk: Int by lazy {
    manifestNode.first("uses-sdk").get<String>("android:minSdkVersion")!!.toInt()
  }

  override fun toString(): String {
    return manifestNode.toString()
  }

  fun writeTo(path: Path) {
    OutputStreamWriter(Files.newOutputStream(path)).use {
      manifestNode.writeTo(it)
    }
  }

  internal fun asTempFile(): File {
    val file = Files.createTempFile("AndroidManifest", ".xml").toFile()
    FileOutputStream(file).writer().use {
      manifestNode.writeTo(it)
    }
    return file
  }

  companion object {
    fun from(path: Path): AndroidManifest {
      return AndroidManifest(parse(Files.newInputStream(path)))
    }

    fun from(xmlSource: String): AndroidManifest {
      return AndroidManifest(parse(xmlSource.byteInputStream()))
    }
  }
}
