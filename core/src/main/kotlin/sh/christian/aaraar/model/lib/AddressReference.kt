package sh.christian.aaraar.model.lib

sealed interface AddressReference {
  object Zero : AddressReference
  data class ProgramHeaderStart(val index: Int) : AddressReference
  data class SectionStart(val index: Int) : AddressReference
}
