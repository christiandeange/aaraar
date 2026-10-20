package sh.christian.aaraar.packaging

import sh.christian.aaraar.Environment
import sh.christian.aaraar.merger.ArchiveMerger
import sh.christian.aaraar.merger.ClassesAndLibsMerger
import sh.christian.aaraar.merger.Glob
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.merger.impl.AarArchiveMerger
import sh.christian.aaraar.merger.impl.AndroidManifestMerger
import sh.christian.aaraar.merger.impl.AndroidManifestMerger.Feature
import sh.christian.aaraar.merger.impl.ApiJarMerger
import sh.christian.aaraar.merger.impl.ArtifactArchiveMerger
import sh.christian.aaraar.merger.impl.AssetsMerger
import sh.christian.aaraar.merger.impl.ClassesMerger
import sh.christian.aaraar.merger.impl.FileSetMerger
import sh.christian.aaraar.merger.impl.GenericJarArchiveMerger
import sh.christian.aaraar.merger.impl.JarArchiveMerger
import sh.christian.aaraar.merger.impl.JniMerger
import sh.christian.aaraar.merger.impl.LintRulesMerger
import sh.christian.aaraar.merger.impl.NavigationJsonMerger
import sh.christian.aaraar.merger.impl.NoJarArchiveMerger
import sh.christian.aaraar.merger.impl.PrefabMerger
import sh.christian.aaraar.merger.impl.ProguardMerger
import sh.christian.aaraar.merger.impl.PublicTxtMerger
import sh.christian.aaraar.merger.impl.RTxtMerger
import sh.christian.aaraar.merger.impl.ResourcesMerger
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.model.ApiJar
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.Assets
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.Jni
import sh.christian.aaraar.model.LintRules
import sh.christian.aaraar.model.NavigationJson
import sh.christian.aaraar.model.Prefab
import sh.christian.aaraar.model.Proguard
import sh.christian.aaraar.model.PublicTxt
import sh.christian.aaraar.model.RTxt
import sh.christian.aaraar.model.Resources

open class DefaultMergerProvider(
  packagingEnvironment: PackagingEnvironment,
) : MergerProvider {
  val jniLibsMergeRules = MergeRules(
    pickFirsts = packagingEnvironment.jniLibs.pickFirsts.toGlob(),
    merges = Glob.None,
    excludes = packagingEnvironment.jniLibs.excludes.toGlob(),
  )

  val resourceMergeRules = MergeRules(
    pickFirsts = packagingEnvironment.resources.pickFirsts.toGlob(),
    merges = packagingEnvironment.resources.merges.toGlob(),
    excludes = packagingEnvironment.resources.excludes.toGlob(),
  )

  val resourcesJarMerger: Merger<GenericJarArchive> = GenericJarArchiveMerger(resourceMergeRules)
  val resourcesFileSetMerger: Merger<FileSet> = FileSetMerger(resourcesJarMerger, resourceMergeRules)
  val jniLibsFileSetMerger: Merger<FileSet> = FileSetMerger(NoJarArchiveMerger, jniLibsMergeRules)
  val prefabFileSetMerger: Merger<FileSet> = FileSetMerger(NoJarArchiveMerger, MergeRules.None)

  override fun androidManifestMerger(): Merger<AndroidManifest> {
    return AndroidManifestMerger(
      Feature.NO_PLACEHOLDER_REPLACEMENT,
      Feature.REMOVE_TOOLS_DECLARATIONS,
      Feature.SKIP_BLAME,
      Feature.SKIP_XML_STRING,
      Feature.USES_SDK_IN_MANIFEST_LENIENT_HANDLING,
    )
  }

  override fun classesAndLibsMerger(): ClassesAndLibsMerger {
    return ClassesMerger(resourcesJarMerger)
  }

  override fun resourcesMerger(): Merger<Resources> {
    return ResourcesMerger()
  }

  override fun rTxtMerger(): Merger<RTxt> {
    return RTxtMerger()
  }

  override fun publicTxtMerger(): Merger<PublicTxt> {
    return PublicTxtMerger()
  }

  override fun assetsMerger(): Merger<Assets> {
    return AssetsMerger(resourcesFileSetMerger)
  }

  override fun jniMerger(): Merger<Jni> {
    return JniMerger(jniLibsFileSetMerger)
  }

  override fun proguardMerger(): Merger<Proguard> {
    return ProguardMerger()
  }

  override fun lintRulesMerger(): Merger<LintRules> {
    return LintRulesMerger(resourcesJarMerger)
  }

  override fun navigationJsonMerger(): Merger<NavigationJson> {
    return NavigationJsonMerger()
  }

  override fun apiJarMerger(): Merger<ApiJar> {
    return ApiJarMerger(resourcesJarMerger)
  }

  override fun prefabMerger(): Merger<Prefab> {
    return PrefabMerger(prefabFileSetMerger)
  }

  override fun aarMerger(): ArchiveMerger<AarArchive> {
    return AarArchiveMerger(
      androidManifestMerger = androidManifestMerger(),
      classesAndLibsMerger = classesAndLibsMerger(),
      resourcesMerger = resourcesMerger(),
      rTxtMerger = rTxtMerger(),
      publicTxtMerger = publicTxtMerger(),
      assetsMerger = assetsMerger(),
      jniMerger = jniMerger(),
      proguardMerger = proguardMerger(),
      lintRulesMerger = lintRulesMerger(),
      navigationJsonMerger = navigationJsonMerger(),
      apiJarMerger = apiJarMerger(),
      prefabMerger = prefabMerger(),
    )
  }

  override fun jarMerger(): ArchiveMerger<JarArchive> {
    return JarArchiveMerger(
      classesMerger = classesAndLibsMerger(),
    )
  }

  override fun archiveMerger(): ArchiveMerger<ArtifactArchive> {
    return ArtifactArchiveMerger(
      jarArchiveMerger = jarMerger(),
      aarArchiveMerger = aarMerger(),
    )
  }

  private fun Collection<String>.toGlob(): Glob {
    return map(Glob::fromString).fold(Glob.None, Glob::plus)
  }

  class Factory : MergerProvider.Factory {
    override fun create(
      environment: Environment,
      packagingEnvironment: PackagingEnvironment,
      shadeEnvironment: ShadeEnvironment,
      logger: PackagerLogger,
    ): MergerProvider {
      return DefaultMergerProvider(
        packagingEnvironment = packagingEnvironment,
      )
    }
  }
}
