package sh.christian.aaraar.model.lib

data class NativeProgramHeader(
  val type: NativeProgramHeaderType,
  val flags: Set<NativeProgramHeaderFlag>,
  val offset: Address,
  val virtualAddress: Address,
  val physicalAddress: Address,
  val segmentFileSize: Value,
  val segmentVirtualSize: Value,
  val alignment: Value,
)
