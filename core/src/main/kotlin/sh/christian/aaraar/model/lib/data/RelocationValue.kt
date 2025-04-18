package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.Address

data class RelocationValue(
  val offset: Address,
  val symbolName: String,
  val type: Int,
)
