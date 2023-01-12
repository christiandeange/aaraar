package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.nio.file.Files
import java.nio.file.Path

class RTxt
private constructor(
  private val symbolTable: SymbolTable,
) {
  operator fun plus(other: RTxt): RTxt {
    return RTxt(symbolTable.merge(other.symbolTable))
  }

  companion object {
    fun from(path: Path, packageName: String): RTxt? {
      if (!Files.isRegularFile(path)) return null

      val symbolTable = SymbolIo.readFromAaptNoValues(
        /* reader */ Files.newBufferedReader(path),
        /* filename */ path.toString(),
        /* tablePackage */ packageName,
      )

      return RTxt(symbolTable)
    }
  }
}
