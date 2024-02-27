package sh.christian.aaraar.gradle

import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.classeditor.Classpath
import java.io.Serializable

/**
 * Receives an incoming, mutable [Classpath] that serves as the reference for an `api.jar` element inside the AAR file.
 *
 * The `api.jar` file is an optional element that contains information about the library's public API.
 * This file helps developers using the library understand its exposed classes, methods, and functionalities.
 * When this file exists in an AAR package, it will be used it as the source of truth for which members are exposed
 * externally by the AAR, and which members can be referenced at compile time.
 *
 * Generating a custom `api.jar` file can be used to hide certain public members from IDE autocomplete, though they
 * can still be referenced and invoked via reflection at runtime as per usual.
 */
abstract class ApiJarProcessor {

  /** Whether the processor is enabled or not. If `false`, no `api.jar` file will be produced. */
  open fun isEnabled(): Boolean = true

  /**
   * Provides the processor with the merged AAR file and a [Classpath] from which an `api.jar` will be based on.
   * The classpath defaults to the public API of [AarArchive.classes], but supports adding/removing/altering classes.
   *
   * This method is only invoked if [isEnabled] is `true`.
   */
  abstract fun processClasspath(
    aarArchive: AarArchive,
    classpath: Classpath,
  )

  interface Factory : Serializable {
    fun create(): ApiJarProcessor
  }
}

fun apiJarProcessorFromClassName(apiJarProcessorFactoryClass: String): ApiJarProcessor.Factory {
  return ClassNameApiJarProcessorFactory(apiJarProcessorFactoryClass)
}

private class ClassNameApiJarProcessorFactory(
  private val apiJarProcessorFactoryClass: String,
) : ApiJarProcessor.Factory {
  private val delegate: ApiJarProcessor.Factory by lazy {
    val apiJarProcessorType = try {
      Class.forName(apiJarProcessorFactoryClass)
    } catch (e: ClassNotFoundException) {
      throw IllegalArgumentException("Couldn't load '$apiJarProcessorFactoryClass' class.", e)
    }

    val constructor = try {
      apiJarProcessorType.getConstructor()
    } catch (e: NoSuchMethodException) {
      throw IllegalArgumentException("No public no-arg constructor on '$apiJarProcessorFactoryClass'.", e)
    }

    constructor.newInstance() as? ApiJarProcessor.Factory
      ?: throw IllegalArgumentException("$apiJarProcessorFactoryClass does not implement ApiJarProcessor.Factory")
  }

  override fun create(): ApiJarProcessor {
    return delegate.create()
  }
}
