package sh.christian.aaraar.gradle

import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.ApiJar
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.classeditor.MutableClasspath

/**
 * Subclass of [ArtifactArchiveProcessor] to allow for producing an `api.jar` element inside an AAR file.
 *
 * The `api.jar` file is an optional element that contains information about the library's public API.
 * This file helps developers using the library understand its exposed classes, methods, and functionalities.
 * When this file exists in an AAR package, it will be used it as the source of truth for which members are exposed
 * externally by the AAR, and which members can be referenced at compile time.
 *
 * Generating a custom `api.jar` file can be used to hide certain public members from IDE autocomplete, though they
 * can still be referenced and invoked via reflection at runtime as per usual.
 *
 * This has no effect if applied to a module that does not produce an Android AAR file.
 */
interface ApiJarProcessor : ArtifactArchiveProcessor {

  /** Whether the processor is enabled or not. If `false`, no `api.jar` file will be produced. */
  fun isEnabled(): Boolean = true

  /**
   * Provides the processor with the merged AAR file and a [MutableClasspath] from which an `api.jar` will be based on.
   * The classpath defaults to the public API of [AarArchive.classes], but supports adding/removing/altering classes.
   *
   * This method is only invoked if [isEnabled] is `true`.
   */
  fun processClasspath(
    aarArchive: AarArchive,
    classpath: MutableClasspath,
  )

  override fun process(
    environment: ProcessorEnvironment,
    archive: ArtifactArchive,
  ): ArtifactArchive {
    // No-op if not enabled or not an AAR archive.
    if (!isEnabled() || archive !is AarArchive) return archive

    val inputApiJar: GenericJarArchive = archive.classes.archive
    val classpath: MutableClasspath = MutableClasspath.from(inputApiJar)

    environment.compileClasspath.forEach { compileDependency ->
      val dependencyArchive = ArtifactArchive.from(compileDependency, environment.environment)
      val dependencyClasspath = when (dependencyArchive) {
        is AarArchive -> {
          MutableClasspath.from(dependencyArchive.apiJar.archive.ifEmpty { dependencyArchive.classes.archive })
        }
        is JarArchive -> {
          MutableClasspath.from(dependencyArchive.classes.archive)
        }
      }
      classpath.addClasspath(dependencyClasspath, addAsInput = false)
    }

    processClasspath(archive, classpath)

    val apiClasses = classpath.apply { asApiJar() }.toGenericJarArchive()
    return archive.copy(apiJar = ApiJar(apiClasses))
  }
}
