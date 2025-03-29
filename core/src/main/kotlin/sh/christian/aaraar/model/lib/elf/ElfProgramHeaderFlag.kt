package sh.christian.aaraar.model.lib.elf

enum class ElfProgramHeaderFlag(val value: Int) {
  Write(0x1),
  Alloc(0x2),
  Exec(0x4),
  ;

  override fun toString(): String {
    return "0x${value.toString(16).padStart(8, '0')}"
  }

  companion object {
    fun from(value: Int): Set<ElfProgramHeaderFlag> {
      return ElfProgramHeaderFlag.values().filter { value and it.value != 0 }.toSet()
    }
  }
}
