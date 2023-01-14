package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.nio.file.Files
import java.nio.file.Path

class PublicTxt
private constructor(
  private val symbolTable: SymbolTable,
) : Mergeable<PublicTxt> {
  override operator fun plus(others: List<PublicTxt>): PublicTxt {
    return PublicTxt(SymbolTable.merge(listOf(symbolTable) + others.map { it.symbolTable }))
  }

  fun writeTo(path: Path) {
    SymbolIo.writeForAar(symbolTable, path)
  }

  companion object {
    fun from(path: Path, packageName: String): PublicTxt {
      if (!Files.isRegularFile(path)) return PublicTxt(symbolTable = SymbolTable.builder().build())

      val symbolTable = SymbolIo.readFromPublicTxtFile(
        /* inputStream */ Files.newInputStream(path),
        /* filename */ path.toString(),
        /* tablePackage */ packageName,
      )

      return PublicTxt(symbolTable)
    }
  }
}
