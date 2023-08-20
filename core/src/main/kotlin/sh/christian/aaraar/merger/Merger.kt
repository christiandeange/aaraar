package sh.christian.aaraar.merger

/**
 * Used for implementations that merge multiple entries together into a target entry, producing a single entry.
 */
interface Merger<T> {
  fun merge(first: T, others: List<T>): T

  fun merge(first: T, other: T): T = merge(first, listOf(other))
}
