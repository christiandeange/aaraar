package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.Value

data class ElfProgramHeader(
  val type: ElfProgramHeaderType,
  val flags: Set<ElfProgramHeaderFlag>,
  val offset: Address,
  val vAddr: Address,
  val pAddr: Address,
  val fileSize: Value,
  val memSize: Value,
  val align: Value,
)
