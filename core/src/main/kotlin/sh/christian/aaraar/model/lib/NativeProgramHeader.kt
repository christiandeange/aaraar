package sh.christian.aaraar.model.lib

data class NativeProgramHeader(
  val type: NativeProgramHeaderType,
  val flags: Set<NativeProgramHeaderFlag>,
  val offset: AddressReference,
  val virtualAddress: Address,
  val segmentFileSize: Value,
  val segmentVirtualSize: Value,
  val alignment: Value,
)
