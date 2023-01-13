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
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class AndroidManifest
private constructor(
  private val document: Document,
) : Mergeable<AndroidManifest> {
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

  override operator fun plus(other: AndroidManifest): AndroidManifest = plus(listOf(other))

  override operator fun plus(others: List<AndroidManifest>): AndroidManifest {
    val mergeReport = newMerger(asTempFile(), StdLogger(Level.WARNING), APPLICATION)
      .withFeatures(Feature.NO_PLACEHOLDER_REPLACEMENT)
      .apply {
        others.forEach { other ->
          addLibraryManifests(other.asTempFile())
        }
      }
      .merge()

    check(mergeReport.result != MergingReport.Result.ERROR) {
      """
        Failed to merge manifest. ${mergeReport.reportString}

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
