package sh.christian.aaraar.model.lib.elf

import sh.christian.aaraar.model.lib.Address

data class ElfFileHeader(
  val identifierMagic: String,
  val identifierClass: ElfFormat,
  val identifierData: ElfEndian,
  val identifierVersion: Byte,
  val identifierOsAbi: Byte,
  val identifierAbiVersion: Byte,
  val identifierType: ElfFileHeaderType,
  val machine: Short,
  val version: Int,
  val entry: Address,
  val phOff: Address,
  val shOff: Address,
  val flags: Int,
  val ehSize: Short,
  val phEntSize: Short,
  val phNum: Short,
  val shEntSize: Short,
  val shNum: Short,
  val shStrNdx: Short,
)
