package sh.christian.aaraar.gradle

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
