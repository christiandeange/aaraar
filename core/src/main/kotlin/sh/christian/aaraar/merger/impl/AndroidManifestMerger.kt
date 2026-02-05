package sh.christian.aaraar.merger.impl

import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.MergingReport
import com.android.utils.StdLogger
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.AndroidManifest

/**
 * Standard implementation for merging multiple `AndroidManifest.xml` files.
 *
 * The basis of this implementation uses the same manifest merging logic that the Android Gradle Plugin uses.
 */
class AndroidManifestMerger : Merger<AndroidManifest> {
  override fun merge(first: AndroidManifest, others: List<AndroidManifest>): AndroidManifest {
    val mergeReport = ManifestMerger2.newMerger(
      first.asTempFile(),
      StdLogger(StdLogger.Level.WARNING),
      ManifestMerger2.MergeType.APPLICATION
    )
      .withFeatures(ManifestMerger2.Invoker.Feature.NO_PLACEHOLDER_REPLACEMENT)
      .withFeatures(ManifestMerger2.Invoker.Feature.REMOVE_TOOLS_DECLARATIONS)
      .withFeatures(ManifestMerger2.Invoker.Feature.USES_SDK_IN_MANIFEST_LENIENT_HANDLING)
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

    return AndroidManifest(mergeReport.getMergedDocument(MergingReport.MergedManifestKind.MERGED)!!)
  }
}
