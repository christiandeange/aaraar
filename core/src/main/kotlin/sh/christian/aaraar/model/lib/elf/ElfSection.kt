package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.Value

data class ElfSection(
  val name: Int,
  val type: ElfSectionType,
  val flags: Set<ElfSectionFlag>,
  val addr: Address,
  val offset: Address,
  val size: Value,
  val link: Int,
  val info: Int,
  val addrAlign: Value,
  val entrySize: Value,
  val data: ElfSectionData,
)
