package sh.christian.aaraar.model

interface Mergeable<T : Mergeable<T>> {
  operator fun plus(others: List<T>): T

  operator fun plus(other: T): T = plus(listOf(other))
}
