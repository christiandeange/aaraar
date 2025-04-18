package sh.christian.aaraar.model.lib

import sh.christian.aaraar.model.lib.data.NativeSectionData

data class NativeSection(
  val name: String,
  val type: NativeSectionType,
  val flags: Set<NativeSectionFlag>,
  val virtualAddress: Address,
  val offset: Address,
  val linkedSectionIndex: Int,
  val extraInfo: Int,
  val alignment: Value,
  val entrySize: Value,
  val data: NativeSectionData,
)
