package sh.christian.aaraar.merger

/**
 * Constraints on how to handle the merging of collections of files.
 *
 * These constraints are consulted primarily when there is more than one entry for a given path. However, [excludes] are
 * also used to exclude any matching entry even if there are no conflicts for that path.
 */
data class MergeRules(
  /**
   * The pattern(s) for which the first occurrence is packaged. Ordering is determined by the order of dependencies.
   */
  val pickFirsts: Glob,
  /**
   * The pattern(s) for which matching resources are merged into a single entry.
   */
  val merges: Glob,
  /**
   * The excluded pattern(s).
   */
  val excludes: Glob,
) {
  companion object {
    val None = MergeRules(
      pickFirsts = Glob.None,
      merges = Glob.None,
      excludes = Glob.None,
    )
  }
}
