package sh.christian.aaraar.shading.impl.transform

internal data class Transformable(
  var data: ByteArray,
  var name: String,
  var time: Long,
) {
  override fun toString(): String {
    return "Transformable(name='$name', time=$time, data=ByteArray[${data.size}])"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Transformable

    if (time != other.time) return false
    if (!data.contentEquals(other.data)) return false
    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    var result = time.hashCode()
    result = 31 * result + data.contentHashCode()
    result = 31 * result + name.hashCode()
    return result
  }
}
