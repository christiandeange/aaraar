package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Value

enum class ElfSectionFlag(val value: Int) {
  Write(0x00000001),
  Alloc(0x00000002),
  Execinstr(0x00000004),
  Merge(0x00000010),
  Strings(0x00000020),
  InfoLink(0x00000040),
  LinkOrder(0x00000080),
  OsNonconforming(0x00000100),
  Group(0x00000200),
  Tls(0x00000400),
  Maskos(0x0FF00000),
  Maskproc(0xF0000000.toInt()),
  Ordered(0x4000000),
  Exclude(0x8000000),
  ;

  override fun toString(): String {
    return "0x${value.toString(16).padStart(8, '0')}"
  }

  companion object {
    fun from(value: Value): Set<ElfSectionFlag> {
      return when (value) {
        is Value.Value32 -> ElfSectionFlag.values().filter { value.value and it.value != 0 }.toSet()
        is Value.Value64 -> ElfSectionFlag.values().filter { value.value.toInt() and it.value != 0 }.toSet()
      }
    }
  }
}
