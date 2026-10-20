package sh.christian.aaraar.packaging

import sh.christian.aaraar.merger.ArchiveMerger
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.model.ApiJar
import sh.christian.aaraar.model.Assets
import sh.christian.aaraar.model.Jni
import sh.christian.aaraar.model.LintRules
import sh.christian.aaraar.model.NavigationJson
import sh.christian.aaraar.model.Prefab
import sh.christian.aaraar.model.Proguard
import sh.christian.aaraar.model.PublicTxt
import sh.christian.aaraar.model.RTxt
import sh.christian.aaraar.model.Resources

/**
 * Creates a merger for a jar archive supported by the packaging pipeline. Only meant to be used in JVM-only libraries.
 * Other merger implementations unused for jar archives are marked as unsupported.
 */
interface JarMergerProvider : MergerProvider {

  override fun androidManifestMerger(): Merger<AndroidManifest> = unsupported()

  override fun resourcesMerger(): Merger<Resources> = unsupported()

  override fun rTxtMerger(): Merger<RTxt> = unsupported()

  override fun publicTxtMerger(): Merger<PublicTxt> = unsupported()

  override fun assetsMerger(): Merger<Assets> = unsupported()

  override fun jniMerger(): Merger<Jni> = unsupported()

  override fun proguardMerger(): Merger<Proguard> = unsupported()

  override fun lintRulesMerger(): Merger<LintRules> = unsupported()

  override fun navigationJsonMerger(): Merger<NavigationJson> = unsupported()

  override fun apiJarMerger(): Merger<ApiJar> = unsupported()

  override fun prefabMerger(): Merger<Prefab> = unsupported()

  override fun aarMerger(): ArchiveMerger<AarArchive> = unsupported()

  private fun unsupported(): Nothing = throw UnsupportedOperationException()
}
