package sh.christian.aaraar.model.lib

enum class NativeProgramHeaderFlag(val value: Int) {
  Write(0x1),
  Alloc(0x2),
  Exec(0x4),
  ;

  override fun toString(): String {
    return "0x${value.toString(16).padStart(8, '0')}"
  }

  companion object {
    fun from(value: Int): Set<NativeProgramHeaderFlag> {
      return NativeProgramHeaderFlag.values().filter { value and it.value != 0 }.toSet()
    }
  }
}
