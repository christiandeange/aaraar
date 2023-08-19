package sh.christian.aaraar.merger

interface Merger<T> {
  fun merge(first: T, others: List<T>): T

  fun merge(first: T, other: T): T = merge(first, listOf(other))
}
