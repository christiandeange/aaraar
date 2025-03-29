package sh.christian.aaraar.model.lib

import sh.christian.aaraar.model.lib.Value.Value32
import sh.christian.aaraar.model.lib.Value.Value64

sealed interface Address {
  data class Address32(val value: Int) : Address {
    override fun toString(): String = "0x${value.toString(16).padStart(8, '0')}"
  }

  data class Address64(val value: Long) : Address {
    override fun toString(): String = "0x${value.toString(16).padStart(16, '0')}"
  }

  operator fun plus(offset: Number): Address {
    return when (this) {
      is Address32 -> Address32(value + offset.toInt())
      is Address64 -> Address64(value + offset.toLong())
    }
  }

  operator fun minus(offset: Number): Address {
    return when (this) {
      is Address32 -> Address32(value - offset.toInt())
      is Address64 -> Address64(value - offset.toLong())
    }
  }

  operator fun minus(address: Address): Value {
    return when (this) {
      is Address32 -> Value32(value - (address as Address32).value)
      is Address64 -> Value64(value - (address as Address64).value)
    }
  }

  operator fun compareTo(address: Address): Int {
    return when (this) {
      is Address32 -> value.compareTo((address as Address32).value)
      is Address64 -> value.compareTo((address as Address64).value)
    }
  }

  fun alignTo(align: Value): Address {
    return when (this) {
      is Address32 -> {
        when (val alignment = (align as Value32).value) {
          0, 1 -> this
          else -> {
            val padding = alignment - (value % alignment)
            if (padding == alignment) this else this + padding
          }
        }
      }
      is Address64 -> {
        when (val alignment = (align as Value64).value) {
          0L, 1L -> this
          else -> {
            val padding = alignment - (value % alignment)
            if (padding == alignment) this else this + padding
          }
        }
      }
    }
  }
}
