package sh.christian.aaraar.utils

fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int =
  if (this is Collection<*>) this.size else default

inline fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> {
  return mapTo(LinkedHashSet(collectionSizeOrDefault(10)), transform)
}
