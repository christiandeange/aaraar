package sh.christian.aaraar.model

import com.android.manifmerger.ManifestMerger2.Invoker.Feature
import com.android.manifmerger.ManifestMerger2.MergeType.APPLICATION
import com.android.manifmerger.ManifestMerger2.newMerger
import com.android.manifmerger.MergingReport
import com.android.manifmerger.MergingReport.MergedManifestKind
import com.android.utils.StdLogger
import com.android.utils.StdLogger.Level
import com.android.utils.childrenIterator
import org.w3c.dom.Document
import org.xml.sax.InputSource
import sh.christian.aaraar.utils.writeTo
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


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

  val minSdk: Int by lazy {
    document.childrenIterator().asSequence()
      .single { it.nodeName == "manifest" }
      .childrenIterator().asSequence()
      .single { it.nodeName == "uses-sdk" }
      .attributes
      .getNamedItem("android:minSdkVersion")
      .textContent
      .toInt()
  }

  operator fun plus(other: AndroidManifest): AndroidManifest {
    val mergeReport = newMerger(asTempFile(), StdLogger(Level.WARNING), APPLICATION)
      .withFeatures(Feature.NO_PLACEHOLDER_REPLACEMENT)
      .addLibraryManifests(other.asTempFile())
      .merge()

    check(mergeReport.result != MergingReport.Result.ERROR) {
      """
        Failed to merge manifest.
        Into: $packageName
        From: ${other.packageName}
        Logs: ${mergeReport.reportString}

        ${mergeReport.loggingRecords.joinToString("\n")}
      """.trimIndent()
    }

    return from(mergeReport.getMergedDocument(MergedManifestKind.MERGED))
  }

  fun writeTo(path: Path) {
    document.writeTo(OutputStreamWriter(Files.newOutputStream(path)))
  }

  private fun asTempFile(): File {
    val file = Files.createTempFile("AndroidManifest", ".xml").toFile()
    document.writeTo(FileWriter(file))
    return file
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

    fun from(xmlSource: String): AndroidManifest {
      val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
      val db: DocumentBuilder = dbf.newDocumentBuilder()
      val document: Document = db.parse(InputSource(StringReader(xmlSource))).apply {
        documentElement.normalize()
      }

      return AndroidManifest(document)
    }
  }
}
