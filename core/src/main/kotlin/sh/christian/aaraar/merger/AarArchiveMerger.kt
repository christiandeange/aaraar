package sh.christian.aaraar.merger

import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.Assets
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.Jni
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.model.LintRules
import sh.christian.aaraar.model.NavigationJson
import sh.christian.aaraar.model.Proguard
import sh.christian.aaraar.model.PublicTxt
import sh.christian.aaraar.model.RTxt
import sh.christian.aaraar.model.Resources

class AarArchiveMerger(
  private val androidManifestMerger: Merger<AndroidManifest>,
  private val classesAndLibsMerger: ClassesAndLibsMerger,
  private val resourcesMerger: Merger<Resources>,
  private val rTxtMerger: Merger<RTxt>,
  private val publicTxtMerger: Merger<PublicTxt>,
  private val assetsMerger: Merger<Assets>,
  private val jniMerger: Merger<Jni>,
  private val proguardMerger: Merger<Proguard>,
  private val lintRulesMerger: Merger<LintRules>,
  private val navigationJsonMerger: Merger<NavigationJson>,
) {
  fun merge(first: AarArchive, others: List<ArtifactArchive>): AarArchive {
    val aars = others.filterIsInstance<AarArchive>()

    // At merging time, jars in the `libs` folder are merged into the `classes.jar` file.
    val allOtherClasses = buildList {
      addAll(first.libs.jars().values.map(::Classes))

      others.forEach { other ->
        when (other) {
          is JarArchive -> {
            add(other.classes)
          }

          is AarArchive -> {
            add(other.classes)
            addAll(other.libs.jars().values.map(::Classes))
          }
        }
      }
    }

    val mergedAndroidManifest = androidManifestMerger.merge(
      first.androidManifest,
      aars.map { it.androidManifest },
    )
    val mergedClasses = classesAndLibsMerger.merge(
      first.classes,
      allOtherClasses,
    )
    val mergedResources = resourcesMerger.merge(
      first.resources,
      aars.map { it.resources },
    )
    val mergedRTxt = rTxtMerger.merge(
      first.rTxt,
      aars.map { it.rTxt },
    )
    val mergedPublicTxt = publicTxtMerger.merge(
      first.publicTxt,
      aars.map { it.publicTxt },
    )
    val mergedAssets = assetsMerger.merge(
      first.assets,
      aars.map { it.assets },
    )
    val mergedJni = jniMerger.merge(
      first.jni,
      aars.map { it.jni },
    )
    val mergedProguard = proguardMerger.merge(
      first.proguard,
      aars.map { it.proguard },
    )
    val mergedLintRules = lintRulesMerger.merge(
      first.lintRules,
      aars.map { it.lintRules },
    )
    val mergedNavigationJson = navigationJsonMerger.merge(
      first.navigationJson,
      aars.map { it.navigationJson },
    )

    return AarArchive(
      aarMetadata = first.aarMetadata,
      androidManifest = mergedAndroidManifest,
      classes = mergedClasses,
      resources = mergedResources,
      rTxt = mergedRTxt,
      publicTxt = mergedPublicTxt,
      assets = mergedAssets,
      libs = Libs.EMPTY,
      jni = mergedJni,
      proguard = mergedProguard,
      lintRules = mergedLintRules,
      navigationJson = mergedNavigationJson,
    )
  }
}
