package sh.christian.aaraar.merger.impl

import com.android.ide.common.symbols.SymbolTable
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.PublicTxt

/**
 * Standard implementation for merging multiple `public.txt` files.
 *
 * The basis of this implementation uses the same resource table merging logic that the Android Gradle Plugin uses.
 */
class PublicTxtMerger : Merger<PublicTxt> {
  override fun merge(first: PublicTxt, others: List<PublicTxt>): PublicTxt {
    return PublicTxt(SymbolTable.merge(listOf(first.symbolTable) + others.map { it.symbolTable }))
  }
}
