package sh.christian.aaraar.model

@Suppress("ReturnCount")
internal fun contentEquals(
  map1: Map<String, ByteArray>,
  map2: Map<String, ByteArray>,
): Boolean {
  if (map1.size != map2.size) return false

  for ((key, value) in map1) {
    val otherValue = map2[key] ?: return false
    if (!value.contentEquals(otherValue)) return false
  }
  return true
}

@Suppress("MagicNumber")
internal fun contentHashCode(
  map: Map<String, ByteArray>,
): Int {
  var result = 1
  for ((key, value) in map) {
    result = 31 * result + key.hashCode()
    result = 31 * result + value.contentHashCode()
  }
  return result
}
