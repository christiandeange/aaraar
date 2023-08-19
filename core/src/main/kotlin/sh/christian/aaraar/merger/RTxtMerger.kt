package sh.christian.aaraar.merger

import com.android.ide.common.symbols.SymbolTable
import sh.christian.aaraar.model.RTxt

class RTxtMerger : Merger<RTxt> {
  override fun merge(first: RTxt, others: List<RTxt>): RTxt {
    return RTxt(SymbolTable.merge(listOf(first.symbolTable) + others.map { it.symbolTable }))
  }
}
