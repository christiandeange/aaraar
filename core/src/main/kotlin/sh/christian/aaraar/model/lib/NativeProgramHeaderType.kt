package sh.christian.aaraar.model.lib

data class NativeProgramHeaderType(val value: Int) {
  override fun toString(): String = when (this) {
    Null -> "Null"
    Load -> "Load"
    Dynamic -> "Dynamic"
    Interp -> "Interp"
    Note -> "Note"
    Shlib -> "Shlib"
    Phdr -> "Phdr"
    Tls -> "Tls"
    else -> "Other(0x${value.toString(16)})"
  }

  companion object {
    val Null = NativeProgramHeaderType(0x00000000)
    val Load = NativeProgramHeaderType(0x00000001)
    val Dynamic = NativeProgramHeaderType(0x00000002)
    val Interp = NativeProgramHeaderType(0x00000003)
    val Note = NativeProgramHeaderType(0x00000004)
    val Shlib = NativeProgramHeaderType(0x00000005)
    val Phdr = NativeProgramHeaderType(0x00000006)
    val Tls = NativeProgramHeaderType(0x00000007)
  }
}
