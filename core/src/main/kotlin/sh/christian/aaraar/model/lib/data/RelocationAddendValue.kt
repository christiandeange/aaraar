package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.Value

data class RelocationAddendValue(
  val offset: Address,
  val symbolName: String,
  val type: Int,
  val addend: Value,
)
