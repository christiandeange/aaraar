package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.NativeProgramHeader
import sh.christian.aaraar.model.lib.NativeProgramHeaderFlag
import sh.christian.aaraar.model.lib.NativeProgramHeaderType
import sh.christian.aaraar.model.lib.Value

data class ElfProgramHeader(
  val p_type: Int,
  val p_flags: Int,
  val p_offset: Address,
  val p_vaddr: Address,
  val p_paddr: Address,
  val p_filesz: Value,
  val p_memsz: Value,
  val p_align: Value,
) {
  fun toNativeProgramHeader(): NativeProgramHeader {
    return NativeProgramHeader(
      type = NativeProgramHeaderType.from(p_type),
      flags = NativeProgramHeaderFlag.from(p_flags),
      offset = p_offset,
      virtualAddress = p_vaddr,
      physicalAddress = p_paddr,
      segmentFileSize = p_filesz,
      segmentVirtualSize = p_memsz,
      alignment = p_align,
    )
  }
}
