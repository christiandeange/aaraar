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

  data class Binding(val value: Byte) {
    override fun toString(): String = when (this) {
      Local -> "Local"
      Global -> "Global"
      Weak -> "Weak"
      else -> "Other(0x${value.toString(16)})"
    }

    companion object {
      fun fromInfo(value: Byte): Binding {
        return Binding((value.toInt() ushr 4).toByte() and 0xF)
      }

      val Local = Binding(0x00)
      val Global = Binding(0x01)
      val Weak = Binding(0x02)
    }
  }

  data class Type(val value: Byte) {
    override fun toString(): String = when (this) {
      NoType -> "NoType"
      Object -> "Object"
      Function -> "Function"
      Section -> "Section"
      File -> "File"
      Common -> "Common"
      SparcRegister -> "SparcRegister"
      else -> "Other(0x${value.toString(16)})"
    }

    companion object {
      fun fromInfo(value: Byte): Type {
        return Type(value and 0xF)
      }

      val NoType = Type(0x00)
      val Object = Type(0x01)
      val Function = Type(0x02)
      val Section = Type(0x03)
      val File = Type(0x04)
      val Common = Type(0x05)
      val SparcRegister = Type(0x0D)
    }
  }
}
