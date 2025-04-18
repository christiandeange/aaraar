package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.Value
import kotlin.experimental.and
import kotlin.experimental.or

data class Symbol(
  val name: String,
  val binding: Binding,
  val type: Type,
  val other: Byte,
  val sectionIndex: Short,
  val value: Value,
  val size: Value,
) {
  val info: Byte
    get() = (binding.value.toInt() shl 4).toByte() or (type.value and 0xF)

  sealed class Binding(val value: Byte) {
    object Local : Binding(0x00)
    object Global : Binding(0x01)
    object Weak : Binding(0x02)
    class Other(value: Byte) : Binding(value)

    companion object {
      fun from(value: Byte): Binding {
        return when (val binding = (value.toInt() ushr 4).toByte() and 0xF) {
          Local.value -> Local
          Global.value -> Global
          Weak.value -> Weak
          else -> Other(binding)
        }
      }
    }
  }

  sealed class Type(val value: Byte) {
    object NoType : Type(0x00)
    object Object : Type(0x01)
    object Function : Type(0x02)
    object Section : Type(0x03)
    object File : Type(0x04)
    object Common : Type(0x05)
    object SparcRegister : Type(0x0D)
    class Other(value: Byte) : Type(value)

    companion object {
      fun from(value: Byte): Type {
        return when (val type = value and 0xF) {
          NoType.value -> NoType
          Object.value -> Object
          Function.value -> Function
          Section.value -> Section
          File.value -> File
          Common.value -> Common
          SparcRegister.value -> SparcRegister
          else -> Other(type)
        }
      }
    }
  }
}
