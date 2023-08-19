package sh.christian.aaraar.merger

import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.Libs

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
