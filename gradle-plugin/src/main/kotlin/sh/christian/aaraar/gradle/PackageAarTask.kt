package sh.christian.aaraar.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import sh.christian.aaraar.Environment
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.ApiJar
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.classeditor.Classpath

@CacheableTask
abstract class PackageAarTask : PackageArchiveTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val inputAar: RegularFileProperty get() = inputArchive

  @get:OutputFile
  val outputAar: RegularFileProperty get() = outputArchive

  @get:Input
  @get:Optional
  abstract val androidAaptIgnore: Property<String>

  @get:Input
  abstract val apiJarProcessorFactory: Property<ApiJarProcessor.Factory>

  final override fun environment(): Environment {
    return Environment(
      androidAaptIgnore = androidAaptIgnore.get(),
      keepClassesMetaFiles = keepMetaFiles.get(),
    )
  }

  override fun postProcessing(archive: ArtifactArchive): ArtifactArchive {
    val aar = archive as AarArchive
    val apiJarProcessor = apiJarProcessorFactory.get().create()

    val outputApiJar = if (apiJarProcessor.isEnabled()) {
      val inputApiJar = aar.classes.archive

      val classpath = Classpath.from(inputApiJar)
      apiJarProcessor.processClasspath(aar, classpath)
      classpath.apply { asApiJar() }.toGenericJarArchive()
    } else {
      GenericJarArchive.NONE
    }

    return AarArchive(
      aarMetadata = aar.aarMetadata,
      androidManifest = aar.androidManifest,
      classes = aar.classes,
      resources = aar.resources,
      rTxt = aar.rTxt,
      publicTxt = aar.publicTxt,
      assets = aar.assets,
      libs = aar.libs,
      jni = aar.jni,
      proguard = aar.proguard,
      lintRules = aar.lintRules,
      navigationJson = aar.navigationJson,
      apiJar = ApiJar.from(outputApiJar),
    )
  }
}
