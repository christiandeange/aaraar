package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.Value

data class DynamicValue(
  val tag: Tag,
  val value: Value,
) {
  sealed class Tag(val value: Int) {
    object Null : Tag(0x00000000)
    object Needed : Tag(0x00000001)
    object PltRelSz : Tag(0x00000002)
    object PltGot : Tag(0x00000003)
    object Hash : Tag(0x00000004)
    object Strtab : Tag(0x00000005)
    object Symtab : Tag(0x00000006)
    object Rela : Tag(0x00000007)
    object RelaSz : Tag(0x00000008)
    object RelaEnt : Tag(0x00000009)
    object StrSz : Tag(0x0000000A)
    object SymEnt : Tag(0x0000000B)
    object Init : Tag(0x0000000C)
    object Fini : Tag(0x0000000D)
    object SoName : Tag(0x0000000E)
    object RPath : Tag(0x0000000F)
    object Symbolic : Tag(0x00000010)
    object Rel : Tag(0x00000011)
    object RelSz : Tag(0x00000012)
    object RelEnt : Tag(0x00000013)
    object PltRel : Tag(0x00000014)
    object Debug : Tag(0x00000015)
    object TextRel : Tag(0x00000016)
    object JmpRel : Tag(0x00000017)
    object BindNow : Tag(0x00000018)
    object InitArray : Tag(0x00000019)
    object FiniArray : Tag(0x0000001A)
    object InitArraySz : Tag(0x0000001B)
    object FiniArraySz : Tag(0x0000001C)
    object RunPath : Tag(0x0000001D)
    object Flags : Tag(0x0000001E)
    object PreinitArray : Tag(0x0000001F)
    object PreinitArraySz : Tag(0x00000100)
    object SunwRtldInf : Tag(0x6000000E)
    object Checksum : Tag(0x6FFFFDF8)
    object PltPadSz : Tag(0x6FFFFDF9)
    object MoveEnt : Tag(0x6FFFFDFA)
    object MoveSz : Tag(0x6FFFFDFB)
    object Feature1 : Tag(0x6FFFFDFC)
    object PosFlag1 : Tag(0x6FFFFDFD)
    object SyminSz : Tag(0x6FFFFDFE)
    object SyminEnt : Tag(0x6FFFFDFF)
    object Config : Tag(0x6FFFFEFA)
    object DepAudit : Tag(0x6FFFFEFB)
    object Audit : Tag(0x6FFFFEFC)
    object PltPad : Tag(0x6FFFFEFD)
    object MoveTab : Tag(0x6FFFFEFE)
    object SymInfo : Tag(0x6FFFFEFF)
    object RelaCount : Tag(0x6FFFFFF9)
    object RelCount : Tag(0x6FFFFFFA)
    object Flags1 : Tag(0x6FFFFFFB)
    object VerDef : Tag(0x6FFFFFFC)
    object VerDefNum : Tag(0x6FFFFFFD)
    object VerNeed : Tag(0x6FFFFFFE)
    object VerNeedNum : Tag(0x6FFFFFFF)
    object SparcRegister : Tag(0x70000001)
    object Auxiliary : Tag(0x7FFFFFFD)
    object Used : Tag(0x7FFFFFFE)
    object Filter : Tag(0x7FFFFFFF)
    class Other(value: Int) : Tag(value)

    companion object {
      fun from(value: Int): Tag = when (value) {
        Null.value -> Null
        Needed.value -> Needed
        PltRelSz.value -> PltRelSz
        PltGot.value -> PltGot
        Hash.value -> Hash
        Strtab.value -> Strtab
        Symtab.value -> Symtab
        Rela.value -> Rela
        RelaSz.value -> RelaSz
        RelaEnt.value -> RelaEnt
        StrSz.value -> StrSz
        SymEnt.value -> SymEnt
        Init.value -> Init
        Fini.value -> Fini
        SoName.value -> SoName
        RPath.value -> RPath
        Symbolic.value -> Symbolic
        Rel.value -> Rel
        RelSz.value -> RelSz
        RelEnt.value -> RelEnt
        PltRel.value -> PltRel
        Debug.value -> Debug
        TextRel.value -> TextRel
        JmpRel.value -> JmpRel
        BindNow.value -> BindNow
        InitArray.value -> InitArray
        FiniArray.value -> FiniArray
        InitArraySz.value -> InitArraySz
        FiniArraySz.value -> FiniArraySz
        RunPath.value -> RunPath
        Flags.value -> Flags
        PreinitArray.value -> PreinitArray
        PreinitArraySz.value -> PreinitArraySz
        SunwRtldInf.value -> SunwRtldInf
        Checksum.value -> Checksum
        PltPadSz.value -> PltPadSz
        MoveEnt.value -> MoveEnt
        MoveSz.value -> MoveSz
        Feature1.value -> Feature1
        PosFlag1.value -> PosFlag1
        SyminSz.value -> SyminSz
        SyminEnt.value -> SyminEnt
        Config.value -> Config
        DepAudit.value -> DepAudit
        Audit.value -> Audit
        PltPad.value -> PltPad
        MoveTab.value -> MoveTab
        SymInfo.value -> SymInfo
        RelaCount.value -> RelaCount
        RelCount.value -> RelCount
        Flags1.value -> Flags1
        VerDef.value -> VerDef
        VerDefNum.value -> VerDefNum
        VerNeed.value -> VerNeed
        VerNeedNum.value -> VerNeedNum
        SparcRegister.value -> SparcRegister
        Auxiliary.value -> Auxiliary
        Used.value -> Used
        Filter.value -> Filter
        else -> Other(value)
      }
    }
  }
}
