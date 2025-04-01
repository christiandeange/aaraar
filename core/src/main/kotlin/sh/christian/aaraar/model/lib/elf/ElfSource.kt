package sh.christian.aaraar.model.lib.elf

import okio.BufferedSource
import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.NativeEndian
import sh.christian.aaraar.model.lib.NativeEndian.BIG
import sh.christian.aaraar.model.lib.NativeEndian.LITTLE
import sh.christian.aaraar.model.lib.NativeFormat
import sh.christian.aaraar.model.lib.NativeFormat.BIT_32
import sh.christian.aaraar.model.lib.NativeFormat.BIT_64
import sh.christian.aaraar.model.lib.Value
import sh.christian.aaraar.model.lib.Value.Value32
import sh.christian.aaraar.model.lib.Value.Value64

class ElfSource(
  private val identifierClass: NativeFormat,
  private val identifierData: NativeEndian,
  private val source: BufferedSource,
) {
  fun byte(): Byte {
    return source.readByte()
  }

  fun bytes(value: Value): ByteArray {
    return source.readByteArray(
      when (value) {
        is Value32 -> value.value.toLong()
        is Value64 -> value.value
      }
    )
  }

  fun short(): Short {
    return when (identifierData) {
      BIG -> source.readShort()
      LITTLE -> source.readShortLe()
    }
  }

  fun int(): Int {
    return when (identifierData) {
      BIG -> source.readInt()
      LITTLE -> source.readIntLe()
    }
  }

  fun long(): Long {
    return when (identifierData) {
      BIG -> source.readLong()
      LITTLE -> source.readLongLe()
    }
  }

  fun address(): Address {
    return when (identifierClass) {
      BIT_32 -> Address32(int())
      BIT_64 -> Address64(long())
    }
  }

  fun value(): Value {
    return when (identifierClass) {
      BIT_32 -> Value32(int())
      BIT_64 -> Value64(long())
    }
  }

  fun skip(count: Long) {
    source.skip(count)
  }
}
