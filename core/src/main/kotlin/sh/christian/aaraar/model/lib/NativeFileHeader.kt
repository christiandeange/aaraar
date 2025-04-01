package sh.christian.aaraar.model.lib

data class NativeFileHeader(
  val architecture: NativeFormat,
  val endianness: NativeEndian,
  val operatingSystemAbi: Byte,
  val operatingSystemAbiVersion: Byte,
  val fileType: NativeFileHeaderType,
  val instructionSet: Short,
  val flags: Int,
  val fileHeaderSize: Short,
  val programHeaderSize: Short,
  val sectionHeaderSize: Short,
  val sectionNamesHeaderIndex: Short,
)
