package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.Jni

class JniMerger(
  private val fileSetMerger: Merger<FileSet>,
) : Merger<Jni> {
  override fun merge(first: Jni, others: List<Jni>): Jni {
    return Jni(fileSetMerger.merge(first.files, others.map { it.files }))
  }
}
