package sh.christian.aaraar.merger.impl

import com.android.ide.common.symbols.SymbolTable
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.RTxt

/**
 * Standard implementation for merging multiple `R.txt` files.
 *
 * Concatenates all rule entries without any sorting or deduplication.
 */
class RTxtMerger : Merger<RTxt> {
  override fun merge(first: RTxt, others: List<RTxt>): RTxt {
    return RTxt(SymbolTable.merge(listOf(first.symbolTable) + others.map { it.symbolTable }))
  }
}
