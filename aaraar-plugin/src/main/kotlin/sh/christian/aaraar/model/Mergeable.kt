package sh.christian.aaraar.model

interface Mergeable<T : Mergeable<T>> {
  operator fun plus(other: T): T

  operator fun plus(others: List<T>): T {
    return others.fold(this) { a, b -> a.plus(b) } as T
  }
}
