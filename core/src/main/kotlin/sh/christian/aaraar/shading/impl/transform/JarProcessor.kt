package sh.christian.aaraar.shading.impl.transform

internal interface JarProcessor {
  enum class Result {
    KEEP,
    DISCARD,
  }

  fun process(struct: Transformable): Result

  companion object {
    const val EXT_CLASS = ".class"
  }
}
