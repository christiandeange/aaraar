package sh.christian.aaraar.utils

import org.w3c.dom.Document
import java.io.Writer
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Document.writeTo(writer: Writer) {
  writer.use {
    val source = DOMSource(this)
    val result = StreamResult(writer)

    val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
    val transformer: Transformer = transformerFactory.newTransformer()
    transformer.transform(source, result)
  }
}
