package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.nio.file.Files
import java.nio.file.Path

class RTxt
internal constructor(
  val symbolTable: SymbolTable,
) {
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
