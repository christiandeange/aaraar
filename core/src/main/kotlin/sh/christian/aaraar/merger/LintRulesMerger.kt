package sh.christian.aaraar.merger

import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.LintRules

class LintRulesMerger(
  private val jarMerger: Merger<GenericJarArchive>,
) : Merger<LintRules> {
  override fun merge(first: LintRules, others: List<LintRules>): LintRules {
    return LintRules(jarMerger.merge(first.archive, others.map { it.archive }))
  }
}
