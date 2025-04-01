package sh.christian.aaraar.model.lib

sealed class NativeProgramHeaderType(val value: Int) {
  object Null : NativeProgramHeaderType(0x00000000)
  object Load : NativeProgramHeaderType(0x00000001)
  object Dynamic : NativeProgramHeaderType(0x00000002)
  object Interp : NativeProgramHeaderType(0x00000003)
  object Note : NativeProgramHeaderType(0x00000004)
  object Shlib : NativeProgramHeaderType(0x00000005)
  object Phdr : NativeProgramHeaderType(0x00000006)
  object Tls : NativeProgramHeaderType(0x00000007)

  class Other(value: Int) : NativeProgramHeaderType(value)

  override fun toString(): String {
    return "0x${value.toString(16).padStart(8, '0')}"
  }

  override fun equals(other: Any?): Boolean {
    return value == (other as? NativeProgramHeaderType)?.value
  }

  override fun hashCode(): Int {
    return value
  }

  companion object {
    fun from(value: Int): NativeProgramHeaderType = when (value) {
      0x00000000 -> Null
      0x00000001 -> Load
      0x00000002 -> Dynamic
      0x00000003 -> Interp
      0x00000004 -> Note
      0x00000005 -> Shlib
      0x00000006 -> Phdr
      0x00000007 -> Tls
      else -> Other(value)
    }
  }
}
