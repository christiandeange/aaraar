package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.AddressReference
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
  fun toNativeProgramHeader(elfSections: List<ElfSection>): NativeProgramHeader {
    val type = NativeProgramHeaderType.from(p_type)
    val offsetSource = when (type) {
      is NativeProgramHeaderType.Phdr -> {
        AddressReference.ProgramHeaderStart(0)
      }
      is NativeProgramHeaderType.Null,
      is NativeProgramHeaderType.Load,
      is NativeProgramHeaderType.Dynamic,
      is NativeProgramHeaderType.Interp,
      is NativeProgramHeaderType.Note,
      is NativeProgramHeaderType.Shlib,
      is NativeProgramHeaderType.Tls,
      is NativeProgramHeaderType.Other -> {
        when (p_offset) {
          Address32(0),
          Address64(0L) -> AddressReference.Zero
          else -> AddressReference.SectionStart(elfSections.indexOfFirst { it.sh_offset == p_offset })
        }
      }
    }

    return NativeProgramHeader(
      type = type,
      flags = NativeProgramHeaderFlag.from(p_flags),
      offset = offsetSource,
      virtualAddress = p_vaddr,
      segmentFileSize = p_filesz,
      segmentVirtualSize = p_memsz,
      alignment = p_align,
    )
  }
}
