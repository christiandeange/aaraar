package sh.christian.aaraar.shading

import com.tonicsystems.jarjar.transform.Transformable
import com.tonicsystems.jarjar.transform.jar.JarProcessor
import com.tonicsystems.jarjar.transform.jar.JarProcessor.Result.*

class ClassFilesProcessor(
  private val jarProcessor: JarProcessor,
) {
  @OptIn(ExperimentalStdlibApi::class)
  fun process(entries: Map<String, ByteArray>): Map<String, ByteArray> = buildMap {
    entries.forEach { (path, contents) ->
      val entry = Transformable().apply {
        name = path
        data = contents
        time = 0L
      }

      when (jarProcessor.process(entry)) {
        KEEP -> put(entry.name, entry.data)
        DISCARD -> Unit
      }
    }
  }
}