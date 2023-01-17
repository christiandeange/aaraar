package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.nio.file.Files
import java.nio.file.Path

class RTxt
private constructor(
  private val symbolTable: SymbolTable,
) : Mergeable<RTxt> {
  override operator fun plus(others: List<RTxt>): RTxt {
    return RTxt(SymbolTable.merge(listOf(symbolTable) + others.map { it.symbolTable }))
  }

  fun writeTo(path: Path) {
    if (symbolTable.symbols.isEmpty) {
      Files.deleteIfExists(path)
    } else {
      SymbolIo.writeForAar(symbolTable, path)
    }
  }

  companion object {
    fun from(path: Path, packageName: String): RTxt {
      if (!Files.isRegularFile(path)) return RTxt(symbolTable = SymbolTable.builder().build())

      val symbolTable = SymbolIo.readFromAaptNoValues(
        /* reader */ Files.newBufferedReader(path),
        /* filename */ path.toString(),
        /* tablePackage */ packageName,
      )

      return RTxt(symbolTable)
    }
  }
}
