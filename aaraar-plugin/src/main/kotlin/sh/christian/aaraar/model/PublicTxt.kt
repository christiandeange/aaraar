package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.nio.file.Files
import java.nio.file.Path

class PublicTxt
private constructor(
  private val symbolTable: SymbolTable,
) {
  operator fun plus(other: PublicTxt): PublicTxt {
    return PublicTxt(symbolTable.merge(other.symbolTable))
  }

  companion object {
    fun from(path: Path, packageName: String): PublicTxt? {
      if (!Files.isRegularFile(path)) return null

      val symbolTable = SymbolIo.readFromPublicTxtFile(
        /* inputStream */ Files.newInputStream(path),
        /* filename */ path.toString(),
        /* tablePackage */ packageName,
      )

      return PublicTxt(symbolTable)
    }
  }
}
