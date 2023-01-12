package sh.christian.aaraar.model

import com.android.utils.childrenIterator
import org.w3c.dom.Document
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class AndroidManifest
private constructor(
  private val document: Document,
) {
  val packageName: String by lazy {
    document.childrenIterator().asSequence()
      .single { it.nodeName == "manifest" }
      .attributes
      .getNamedItem("package")
      .textContent
  }

  companion object {
    fun from(path: Path): AndroidManifest {
      val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
      val db: DocumentBuilder = dbf.newDocumentBuilder()
      val document: Document = db.parse(Files.newInputStream(path)).apply {
        documentElement.normalize()
      }

      return AndroidManifest(document)
    }
  }
}
