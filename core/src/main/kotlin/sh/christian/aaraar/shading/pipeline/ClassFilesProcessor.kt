package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.transform.Transformable
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor.Result.DISCARD
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor.Result.KEEP

internal class ClassFilesProcessor(
  private val jarProcessor: JarProcessor,
) {
  fun process(entries: Map<String, ByteArray>): Map<String, ByteArray> = buildMap {
    entries.forEach { (path, contents) ->
      val entry = Transformable(
        name = path,
        data = contents,
        time = 0L,
      )

      when (jarProcessor.process(entry)) {
        KEEP -> put(entry.name, entry.data)
        DISCARD -> Unit
      }
    }
  }
}
