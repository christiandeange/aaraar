package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.Proguard

/**
 * Standard implementation for merging multiple proguard rule files.
 *
 * Concatenates all rule entries without any deduplication.
 */
class ProguardMerger : Merger<Proguard> {
  override fun merge(first: Proguard, others: List<Proguard>): Proguard {
    return Proguard(first.lines + others.flatMap { it.lines })
  }
}
