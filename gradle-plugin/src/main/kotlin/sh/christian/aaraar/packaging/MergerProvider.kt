package sh.christian.aaraar.packaging

import sh.christian.aaraar.Environment
import sh.christian.aaraar.merger.ArchiveMerger
import sh.christian.aaraar.merger.ClassesAndLibsMerger
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.model.ApiJar
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.Assets
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.Jni
import sh.christian.aaraar.model.LintRules
import sh.christian.aaraar.model.NavigationJson
import sh.christian.aaraar.model.Proguard
import sh.christian.aaraar.model.PublicTxt
import sh.christian.aaraar.model.RTxt
import sh.christian.aaraar.model.Resources
import java.io.Serializable

/**
 * Creates mergers for the archive components supported by the packaging pipeline.
 */
interface MergerProvider {

  /**
   * Creates a merger for Android manifests.
   */
  fun androidManifestMerger(): Merger<AndroidManifest>

  /**
   * Creates a merger for classes and library archives.
   */
  fun classesAndLibsMerger(): ClassesAndLibsMerger

  /**
   * Creates a merger for Android resources.
   */
  fun resourcesMerger(): Merger<Resources>

  /**
   * Creates a merger for compiled resource symbol files.
   */
  fun rTxtMerger(): Merger<RTxt>

  /**
   * Creates a merger for public resource symbol files.
   */
  fun publicTxtMerger(): Merger<PublicTxt>

  /**
   * Creates a merger for Android asset files.
   */
  fun assetsMerger(): Merger<Assets>

  /**
   * Creates a merger for JNI libraries.
   */
  fun jniMerger(): Merger<Jni>

  /**
   * Creates a merger for ProGuard configuration files.
   */
  fun proguardMerger(): Merger<Proguard>

  /**
   * Creates a merger for lint rule files.
   */
  fun lintRulesMerger(): Merger<LintRules>

  /**
   * Creates a merger for navigation JSON files.
   */
  fun navigationJsonMerger(): Merger<NavigationJson>

  /**
   * Creates a merger for API JAR archives.
   */
  fun apiJarMerger(): Merger<ApiJar>

  /**
   * Creates a merger for Android AAR archives.
   */
  fun aarMerger(): ArchiveMerger<AarArchive>

  /**
   * Creates a merger for JAR archives.
   */
  fun jarMerger(): ArchiveMerger<JarArchive>

  /**
   * Creates a merger for JAR or Android AAR archives.
   */
  fun archiveMerger(): ArchiveMerger<ArtifactArchive>

  interface Factory : Serializable {
    fun create(
      environment: Environment,
      packagingEnvironment: PackagingEnvironment,
      shadeEnvironment: ShadeEnvironment,
      logger: PackagerLogger,
    ): MergerProvider
  }
}
