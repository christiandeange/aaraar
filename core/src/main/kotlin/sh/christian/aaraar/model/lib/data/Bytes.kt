package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.NativeLibrary
import sh.christian.aaraar.model.lib.NativeSection

data class Bytes(
  val data: ByteArray,
) : NativeSectionData {
  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray = data

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Bytes

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return "Bytes(data=[${data.size} bytes])"
  }
}
