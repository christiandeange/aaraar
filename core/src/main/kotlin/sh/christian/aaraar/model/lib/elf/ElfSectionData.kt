package sh.christian.aaraar.model.lib.elf

data class ElfSectionData(
  val data: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ElfSectionData

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return "ElfSectionData(data=[${data.size} bytes])"
  }
}
