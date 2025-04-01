package sh.christian.aaraar.model.lib

data class NativeSectionData(
  val data: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as NativeSectionData

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return "NativeSectionData(data=[${data.size} bytes])"
  }
}
