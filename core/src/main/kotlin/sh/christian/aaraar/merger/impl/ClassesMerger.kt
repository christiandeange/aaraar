package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.ClassesAndLibsMerger
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.Libs

/**
 * Standard jar-wise implementation for merging multiple `classes.jar` files.
 */
class ClassesMerger(
  private val jarMerger: Merger<GenericJarArchive>,
) : ClassesAndLibsMerger {
  override fun merge(first: Classes, others: List<Classes>): Classes {
    return Classes(jarMerger.merge(first.archive, others.map { it.archive }))
  }

  override fun merge(first: Classes, others: Libs): Classes {
    return Classes(jarMerger.merge(first.archive, others.jars().values.toList()))
  }
}
