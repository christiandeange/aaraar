package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.Value

data class DynamicValue(
  val tag: Tag,
  val value: Value,
) {
  data class Tag(val value: Int) {
    override fun toString(): String = when (this) {
      Null -> "Null"
      Needed -> "Needed"
      PltRelSz -> "PltRelSz"
      PltGot -> "PltGot"
      Hash -> "Hash"
      Strtab -> "Strtab"
      Symtab -> "Symtab"
      Rela -> "Rela"
      RelaSz -> "RelaSz"
      RelaEnt -> "RelaEnt"
      StrSz -> "StrSz"
      SymEnt -> "SymEnt"
      Init -> "Init"
      Fini -> "Fini"
      SoName -> "SoName"
      RPath -> "RPath"
      Symbolic -> "Symbolic"
      Rel -> "Rel"
      RelSz -> "RelSz"
      RelEnt -> "RelEnt"
      PltRel -> "PltRel"
      Debug -> "Debug"
      TextRel -> "TextRel"
      JmpRel -> "JmpRel"
      BindNow -> "BindNow"
      InitArray -> "InitArray"
      FiniArray -> "FiniArray"
      InitArraySz -> "InitArraySz"
      FiniArraySz -> "FiniArraySz"
      RunPath -> "RunPath"
      Flags -> "Flags"
      PreinitArray -> "PreinitArray"
      PreinitArraySz -> "PreinitArraySz"
      SunwRtldInf -> "SunwRtldInf"
      Checksum -> "Checksum"
      PltPadSz -> "PltPadSz"
      MoveEnt -> "MoveEnt"
      MoveSz -> "MoveSz"
      Feature1 -> "Feature1"
      PosFlag1 -> "PosFlag1"
      SyminSz -> "SyminSz"
      SyminEnt -> "SyminEnt"
      Config -> "Config"
      DepAudit -> "DepAudit"
      Audit -> "Audit"
      PltPad -> "PltPad"
      MoveTab -> "MoveTab"
      SymInfo -> "SymInfo"
      RelaCount -> "RelaCount"
      RelCount -> "RelCount"
      Flags1 -> "Flags1"
      VerDef -> "VerDef"
      VerDefNum -> "VerDefNum"
      VerNeed -> "VerNeed"
      VerNeedNum -> "VerNeedNum"
      SparcRegister -> "SparcRegister"
      Auxiliary -> "Auxiliary"
      Used -> "Used"
      Filter -> "Filter"
      else -> "Other(0x${value.toString(16)})"
    }

    companion object {
      val Null = Tag(0x00000000)
      val Needed = Tag(0x00000001)
      val PltRelSz = Tag(0x00000002)
      val PltGot = Tag(0x00000003)
      val Hash = Tag(0x00000004)
      val Strtab = Tag(0x00000005)
      val Symtab = Tag(0x00000006)
      val Rela = Tag(0x00000007)
      val RelaSz = Tag(0x00000008)
      val RelaEnt = Tag(0x00000009)
      val StrSz = Tag(0x0000000A)
      val SymEnt = Tag(0x0000000B)
      val Init = Tag(0x0000000C)
      val Fini = Tag(0x0000000D)
      val SoName = Tag(0x0000000E)
      val RPath = Tag(0x0000000F)
      val Symbolic = Tag(0x00000010)
      val Rel = Tag(0x00000011)
      val RelSz = Tag(0x00000012)
      val RelEnt = Tag(0x00000013)
      val PltRel = Tag(0x00000014)
      val Debug = Tag(0x00000015)
      val TextRel = Tag(0x00000016)
      val JmpRel = Tag(0x00000017)
      val BindNow = Tag(0x00000018)
      val InitArray = Tag(0x00000019)
      val FiniArray = Tag(0x0000001A)
      val InitArraySz = Tag(0x0000001B)
      val FiniArraySz = Tag(0x0000001C)
      val RunPath = Tag(0x0000001D)
      val Flags = Tag(0x0000001E)
      val PreinitArray = Tag(0x0000001F)
      val PreinitArraySz = Tag(0x00000100)
      val SunwRtldInf = Tag(0x6000000E)
      val Checksum = Tag(0x6FFFFDF8)
      val PltPadSz = Tag(0x6FFFFDF9)
      val MoveEnt = Tag(0x6FFFFDFA)
      val MoveSz = Tag(0x6FFFFDFB)
      val Feature1 = Tag(0x6FFFFDFC)
      val PosFlag1 = Tag(0x6FFFFDFD)
      val SyminSz = Tag(0x6FFFFDFE)
      val SyminEnt = Tag(0x6FFFFDFF)
      val Config = Tag(0x6FFFFEFA)
      val DepAudit = Tag(0x6FFFFEFB)
      val Audit = Tag(0x6FFFFEFC)
      val PltPad = Tag(0x6FFFFEFD)
      val MoveTab = Tag(0x6FFFFEFE)
      val SymInfo = Tag(0x6FFFFEFF)
      val RelaCount = Tag(0x6FFFFFF9)
      val RelCount = Tag(0x6FFFFFFA)
      val Flags1 = Tag(0x6FFFFFFB)
      val VerDef = Tag(0x6FFFFFFC)
      val VerDefNum = Tag(0x6FFFFFFD)
      val VerNeed = Tag(0x6FFFFFFE)
      val VerNeedNum = Tag(0x6FFFFFFF)
      val SparcRegister = Tag(0x70000001)
      val Auxiliary = Tag(0x7FFFFFFD)
      val Used = Tag(0x7FFFFFFE)
      val Filter = Tag(0x7FFFFFFF)
    }
  }
}
