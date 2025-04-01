package sh.christian.aaraar.model.lib

data class NativeSection(
  val nameOffset: Int,
  val type: NativeSectionType,
  val flags: Set<NativeSectionFlag>,
  val virtualAddress: Address,
  val offset: Address,
  val size: Value,
  val linkedSectionIndex: Int,
  val extraInfo: Int,
  val alignment: Value,
  val entrySize: Value,
  val data: NativeSectionData,
)
