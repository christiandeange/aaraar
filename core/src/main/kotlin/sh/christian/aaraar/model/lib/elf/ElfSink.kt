package sh.christian.aaraar.model.lib.elf

import okio.BufferedSink
import sh.christian.aaraar.model.lib.Address
import sh.christian.aaraar.model.lib.Address.Address32
import sh.christian.aaraar.model.lib.Address.Address64
import sh.christian.aaraar.model.lib.NativeEndian
import sh.christian.aaraar.model.lib.NativeEndian.BIG
import sh.christian.aaraar.model.lib.NativeEndian.LITTLE
import sh.christian.aaraar.model.lib.Value
import sh.christian.aaraar.model.lib.Value.Value32
import sh.christian.aaraar.model.lib.Value.Value64

class ElfSink(
  private val identifierData: NativeEndian,
  private val bufferedSink: BufferedSink,
) {
  fun byte(byte: Byte) {
    bufferedSink.writeByte(byte.toInt())
  }

  fun bytes(byteArray: ByteArray) {
    bufferedSink.write(byteArray)
  }

  fun short(short: Short) {
    when (identifierData) {
      BIG -> bufferedSink.writeShort(short.toInt())
      LITTLE -> bufferedSink.writeShortLe(short.toInt())
    }
  }

  fun int(int: Int) {
    when (identifierData) {
      BIG -> bufferedSink.writeInt(int)
      LITTLE -> bufferedSink.writeIntLe(int)
    }
  }

  fun long(long: Long) {
    when (identifierData) {
      BIG -> bufferedSink.writeLong(long)
      LITTLE -> bufferedSink.writeLongLe(long)
    }
  }

  fun address(address: Address) {
    when (address) {
      is Address32 -> int(address.value)
      is Address64 -> long(address.value)
    }
  }

  fun value(value: Value) {
    when (value) {
      is Value32 -> int(value.value)
      is Value64 -> long(value.value)
    }
  }

  fun skip(count: Int) {
    bytes(ByteArray(count))
  }

  fun skip(count: Long) {
    bytes(ByteArray(count.toInt()))
  }

  fun skipTo(address: Address) {
    val currentAddr = when (address) {
      is Address32 -> Address32(bufferedSink.buffer.size.toInt())
      is Address64 -> Address64(bufferedSink.buffer.size)
    }

    check(address >= currentAddr) {
      "Cannot go backwards to address $address, current address is $currentAddr"
    }

    when (val delta = address - currentAddr) {
      is Value32 -> skip(delta.value)
      is Value64 -> skip(delta.value)
    }
  }
}
