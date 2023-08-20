package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.LintRules

/**
 * Standard jar-wise implementation for merging multiple `lint.jar` files.
 */
class LintRulesMerger(
  private val jarMerger: Merger<GenericJarArchive>,
) : Merger<LintRules> {
  override fun merge(first: LintRules, others: List<LintRules>): LintRules {
    return LintRules(jarMerger.merge(first.archive, others.map { it.archive }))
  }
}
