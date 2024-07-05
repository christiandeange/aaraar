package sh.christian.aaraar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.LibraryBinaryIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier
import sh.christian.aaraar.Environment
import sh.christian.aaraar.gradle.ShadeConfigurationScope.All
import sh.christian.aaraar.gradle.ShadeConfigurationScope.AnyScope
import sh.christian.aaraar.gradle.ShadeConfigurationScope.DependencyScope
import sh.christian.aaraar.gradle.ShadeConfigurationScope.FilesScope
import sh.christian.aaraar.gradle.ShadeConfigurationScope.ProjectScope
import sh.christian.aaraar.merger.Glob
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.merger.impl.AarArchiveMerger
import sh.christian.aaraar.merger.impl.AndroidManifestMerger
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
import sh.christian.aaraar.merger.impl.ProguardMerger
import sh.christian.aaraar.merger.impl.PublicTxtMerger
import sh.christian.aaraar.merger.impl.RTxtMerger
import sh.christian.aaraar.merger.impl.ResourcesMerger
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.shading.impl.AarArchiveShader
import sh.christian.aaraar.shading.impl.ClassesShader
import sh.christian.aaraar.shading.impl.GenericJarArchiveShader
import sh.christian.aaraar.shading.impl.JarArchiveShader
import sh.christian.aaraar.shading.impl.LibsShader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class PackageArchiveTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(RELATIVE)
  abstract val inputArchive: RegularFileProperty

  @get:Classpath
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val embedClasspath: Property<Configuration>

  @get:Input
  abstract val shadeEnvironment: Property<ShadeEnvironment>

  @get:Input
  abstract val packagingEnvironment: Property<PackagingEnvironment>

  @get:Input
  abstract val keepMetaFiles: Property<Boolean>

  @get:OutputFile
  abstract val outputArchive: RegularFileProperty

  internal abstract fun environment(): Environment

  @TaskAction
  fun packageArtifactArchive() {
    val environment = environment()
    logger.info("Packaging environment: $environment")

    val componentMapping: Map<Path, ComponentIdentifier> = embedClasspath.get()
      .incoming.artifacts.resolvedArtifacts.get()
      .associate {
        it.file.toPath() to it.id.componentIdentifier
      }

    val inputPath = inputArchive.getPath()
    val shadeEnvironment = shadeEnvironment.get()

    logger.info("Merge base: $inputPath")
    val input = ArtifactArchive.from(inputPath, environment).applyShading(
      path = inputPath,
      shadeEnvironment = shadeEnvironment,
      identifier = ProjectScope(identityPath.parent!!.toString()),
    )

    val dependencyArchives =
      componentMapping.keys
        .minus(inputPath)
        .map { archivePath ->
          val dependencyId = componentMapping[archivePath]
          val identifier = dependencyId?.toShadeConfigurationScope()

          logger.info("Processing $dependencyId (id=$identifier)")
          logger.info("  Input file: $archivePath")
          ArtifactArchive.from(archivePath, environment).applyShading(
            path = archivePath,
            shadeEnvironment = shadeEnvironment,
            identifier = identifier,
          )
        }

    val mergedArchive = mergeArchive(input, dependencyArchives, packagingEnvironment.get())
    val finalizedArchive = postProcessing(mergedArchive)

    val outputPath = outputArchive.getPath().apply { Files.deleteIfExists(this) }
    logger.info("Merged into: $outputPath")
    finalizedArchive.writeTo(path = outputPath)
  }

  open fun postProcessing(archive: ArtifactArchive): ArtifactArchive {
    // No-op by default.
    return archive
  }

  private fun mergeArchive(
    inputArchive: ArtifactArchive,
    dependencyArchives: List<ArtifactArchive>,
    packagingEnvironment: PackagingEnvironment,
  ): ArtifactArchive {
    fun Collection<String>.toGlob(): Glob = map(Glob::fromString).fold(Glob.None, Glob::plus)

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

    val resourcesJarMerger = GenericJarArchiveMerger(resourceMergeRules)
    val resourcesFileSetMerger = FileSetMerger(resourcesJarMerger, resourceMergeRules)
    val jniLibsFileSetMerger = FileSetMerger(NoJarArchiveMerger, jniLibsMergeRules)

    val artifactArchiveMerger = ArtifactArchiveMerger(
      jarArchiveMerger = JarArchiveMerger(
        classesMerger = ClassesMerger(resourcesJarMerger),
      ),
      aarArchiveMerger = AarArchiveMerger(
        androidManifestMerger = AndroidManifestMerger(),
        classesAndLibsMerger = ClassesMerger(resourcesJarMerger),
        resourcesMerger = ResourcesMerger(),
        rTxtMerger = RTxtMerger(),
        publicTxtMerger = PublicTxtMerger(),
        assetsMerger = AssetsMerger(resourcesFileSetMerger),
        jniMerger = JniMerger(jniLibsFileSetMerger),
        proguardMerger = ProguardMerger(),
        lintRulesMerger = LintRulesMerger(resourcesJarMerger),
        navigationJsonMerger = NavigationJsonMerger(),
        apiJarMerger = ApiJarMerger(resourcesJarMerger),
      ),
    )

    return artifactArchiveMerger.merge(inputArchive, dependencyArchives)
  }

  private fun RegularFileProperty.getPath(): Path {
    return get().asFile.toPath()
  }

  private fun ArtifactArchive.applyShading(
    path: Path,
    shadeEnvironment: ShadeEnvironment,
    identifier: ShadeConfigurationScope?,
  ): ArtifactArchive {
    val emptyConfiguration = ShadeConfiguration(
      classRenames = emptyMap(),
      classDeletes = emptySet(),
      resourceExclusions = emptySet(),
    )

    val shadeRules = shadeEnvironment.rules
      .filter { rule -> rule.scope.matches(path, shadeEnvironment, identifier) }
      .fold(emptyConfiguration) { a, b ->
        ShadeConfiguration(
          classRenames = a.classRenames + b.configuration.classRenames,
          classDeletes = a.classDeletes + b.configuration.classDeletes,
          resourceExclusions = a.resourceExclusions + b.configuration.resourceExclusions,
        )
      }

    return if (shadeRules.isEmpty()) {
      this
    } else {
      logger.info("  Applying shading rules:")
      shadeRules.classRenames.forEach { (pattern, result) ->
        logger.info("    Rename class '$pattern' → '$result'")
      }
      shadeRules.classDeletes.forEach { target ->
        logger.info("    Delete class '$target'")
      }
      shadeRules.resourceExclusions.forEach { target ->
        logger.info("    Remove file  '$target'")
      }

      val genericJarArchiveShader = GenericJarArchiveShader()
      val classesShader = ClassesShader(genericJarArchiveShader)
      val libsShader = LibsShader(genericJarArchiveShader)
      when (this) {
        is AarArchive -> AarArchiveShader(classesShader, libsShader).shade(this, shadeRules)
        is JarArchive -> JarArchiveShader(classesShader).shade(this, shadeRules)
      }
    }
  }

  private fun ShadeConfigurationScope.matches(
    path: Path,
    shadeEnvironment: ShadeEnvironment,
    identifier: ShadeConfigurationScope?,
  ): Boolean = when (this) {
    is All -> true
    is DependencyScope -> identifier is DependencyScope && matches(identifier)
    is ProjectScope -> identifier is ProjectScope && matches(identifier)
    is FilesScope -> matches(path.toFile())
    is AnyScope -> scopes.any { it.matches(path, shadeEnvironment, identifier) }
  }

  private fun DependencyScope.matches(identifier: DependencyScope): Boolean {
    val matchesGroup = group == identifier.group
    val matchesName = name == null || name == identifier.name
    val matchesVersion = version == null || version == identifier.version

    return matchesGroup && matchesName && matchesVersion
  }

  private fun ProjectScope.matches(identifier: ProjectScope): Boolean {
    return path == identifier.path
  }

  private fun FilesScope.matches(path: File): Boolean {
    return path in files
  }

  private fun ComponentIdentifier.toShadeConfigurationScope(): ShadeConfigurationScope? = when (this) {
    is ModuleComponentIdentifier -> DependencyScope(group, module, version)
    is ProjectComponentIdentifier -> ProjectScope(projectPath)
    is OpaqueComponentArtifactIdentifier -> FilesScope(setOf(file))
    is LibraryBinaryIdentifier -> null
    else -> null
  }
}
