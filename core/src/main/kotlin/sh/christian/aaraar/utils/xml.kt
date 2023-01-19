package sh.christian.aaraar.utils

import com.android.SdkConstants.XMLNS_PREFIX
import com.android.utils.forEach
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.node
import org.w3c.dom.Node as W3CNode

fun W3CNode.toNode(): Node {
  return node(nodeName) {
    copyNode(this@toNode, this)
  }.first(nodeName)
}

private fun copyNode(source: W3CNode, dest: Node) {
  when (source.nodeType) {
    W3CNode.ELEMENT_NODE -> {
      val cur = dest.element(source.nodeName)
      copyAttributes(source, cur)
      source.childNodes.forEach { child -> copyNode(child, cur) }
    }

    W3CNode.CDATA_SECTION_NODE -> {
      dest.cdata(source.nodeValue)
    }

    W3CNode.TEXT_NODE -> {
      dest.text(source.nodeValue)
    }

    W3CNode.COMMENT_NODE -> {
      dest.comment(source.nodeValue)
    }
  }
}

private fun copyAttributes(source: W3CNode, dest: Node) {
  val attributes = source.attributes
  if (attributes == null || attributes.length == 0) {
    return
  }

  attributes.forEach {
    if (it.nodeName.startsWith(XMLNS_PREFIX)) {
      dest.namespace(it.nodeName.removePrefix(XMLNS_PREFIX), it.nodeValue)
    } else {
      dest.attribute(it.nodeName, it.nodeValue)
    }
  }
}
