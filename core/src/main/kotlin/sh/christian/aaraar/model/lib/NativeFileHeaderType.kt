package sh.christian.aaraar.model.lib

data class NativeFileHeaderType(val value: Short) {
  override fun toString(): String = when (this) {
    None -> "None"
    Rel -> "Rel"
    Exec -> "Exec"
    Dyn -> "Dyn"
    Core -> "Core"
    else -> "Other(0x${value.toString(16)})"
  }

  companion object {
    val None = NativeFileHeaderType(0x0000.toShort())
    val Rel = NativeFileHeaderType(0x0001.toShort())
    val Exec = NativeFileHeaderType(0x0002.toShort())
    val Dyn = NativeFileHeaderType(0x0003.toShort())
    val Core = NativeFileHeaderType(0x0004.toShort())
  }
}
