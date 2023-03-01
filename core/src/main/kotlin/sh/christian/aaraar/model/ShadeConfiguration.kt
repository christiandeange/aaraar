package sh.christian.aaraar.model

import java.io.Serializable

data class ShadeConfiguration(
  val classRenames: Map<String, String>,
  val classDeletes: Set<String>,
  val resourceExclusions: Set<String>,
) : Serializable {
  fun isEmpty(): Boolean {
    return classRenames.isEmpty() && classDeletes.isEmpty() && resourceExclusions.isEmpty()
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
