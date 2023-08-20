package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the contents of the `public.txt` file.
 */
class PublicTxt
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
