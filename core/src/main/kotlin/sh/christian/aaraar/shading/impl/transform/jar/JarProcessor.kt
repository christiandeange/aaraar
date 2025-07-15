package sh.christian.aaraar.shading.impl.transform.jar

import sh.christian.aaraar.shading.impl.transform.Transformable
import java.io.IOException

internal interface JarProcessor {
  enum class Result {
    KEEP,
    DISCARD
  }

  @Throws(IOException::class)
  fun scan(struct: Transformable): Result

  /**
   * Process the entry (e.g. rename the file)
   *
   * Returns `true` if the processor wants to retain the entry. In this case, the entry can be removed
   * from the jar file in a future time. Return `false` for the entries which do not have been changed and
   * therefore are not to be deleted
   *
   * @param struct The archive entry to be transformed.
   * @return `true` if he process chain can continue after this process
   * @throws IOException if it all goes upside down
   */
  @Throws(IOException::class)
  fun process(struct: Transformable): Result

  companion object {
    const val EXT_CLASS = ".class"
  }
}
