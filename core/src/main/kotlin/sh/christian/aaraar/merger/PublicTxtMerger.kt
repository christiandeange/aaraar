package sh.christian.aaraar.merger

import com.android.ide.common.symbols.SymbolTable
import sh.christian.aaraar.model.PublicTxt

class PublicTxtMerger : Merger<PublicTxt> {
  override fun merge(first: PublicTxt, others: List<PublicTxt>): PublicTxt {
    return PublicTxt(SymbolTable.merge(listOf(first.symbolTable) + others.map { it.symbolTable }))
  }
}
