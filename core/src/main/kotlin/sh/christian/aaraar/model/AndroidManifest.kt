package sh.christian.aaraar.model

import com.android.manifmerger.ManifestMerger2.Invoker.Feature
import com.android.manifmerger.ManifestMerger2.MergeType.APPLICATION
import com.android.manifmerger.ManifestMerger2.newMerger
import com.android.manifmerger.MergingReport
import com.android.manifmerger.MergingReport.MergedManifestKind
import com.android.utils.StdLogger
import com.android.utils.StdLogger.Level
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.parse
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path

class AndroidManifest
private constructor(
  private val manifestNode: Node,
) : Mergeable<AndroidManifest> {
  val packageName: String by lazy {
    manifestNode.get<String>("package")!!
  }

  val minSdk: Int by lazy {
    manifestNode.first("uses-sdk").get<String>("android:minSdkVersion")!!.toInt()
  }

  override operator fun plus(others: List<AndroidManifest>): AndroidManifest {
    val mergeReport = newMerger(asTempFile(), StdLogger(Level.WARNING), APPLICATION)
      .withFeatures(Feature.NO_PLACEHOLDER_REPLACEMENT)
      .apply {
        others.forEach { other ->
          addLibraryManifest(other.asTempFile())
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
    OutputStreamWriter(Files.newOutputStream(path)).use {
      manifestNode.writeTo(it)
    }
  }

  private fun asTempFile(): File {
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
