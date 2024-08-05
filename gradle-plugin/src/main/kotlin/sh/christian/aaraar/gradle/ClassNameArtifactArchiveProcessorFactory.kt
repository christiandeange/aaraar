package sh.christian.aaraar.gradle

fun artifactArchiveProcessorFromClassName(processorFactoryClass: String): ArtifactArchiveProcessor.Factory {
  return ClassNameArtifactArchiveProcessorFactory(processorFactoryClass)
}

private class ClassNameArtifactArchiveProcessorFactory(
  private val processorFactoryClass: String,
) : ArtifactArchiveProcessor.Factory {
  private val delegate: ArtifactArchiveProcessor.Factory by lazy {
    val apiJarProcessorType = try {
      Class.forName(processorFactoryClass)
    } catch (e: ClassNotFoundException) {
      throw IllegalArgumentException("Couldn't load '$processorFactoryClass' class.", e)
    }

    val constructor = try {
      apiJarProcessorType.getConstructor()
    } catch (e: NoSuchMethodException) {
      throw IllegalArgumentException("No public no-arg constructor on '$processorFactoryClass'.", e)
    }

    constructor.newInstance() as? ArtifactArchiveProcessor.Factory
      ?: throw IllegalArgumentException("$processorFactoryClass does not implement ArtifactArchiveProcessor.Factory")
  }

  override fun create(): ArtifactArchiveProcessor {
    return delegate.create()
  }
}
