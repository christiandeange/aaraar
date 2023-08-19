package sh.christian.aaraar.merger

import sh.christian.aaraar.model.Proguard

class ProguardMerger : Merger<Proguard> {
  override fun merge(first: Proguard, others: List<Proguard>): Proguard {
    return Proguard(first.lines + others.flatMap { it.lines })
  }
}
