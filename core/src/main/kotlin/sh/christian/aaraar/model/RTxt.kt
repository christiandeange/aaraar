package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the contents of the `R.txt` file.
 */
class RTxt
internal constructor(
  val symbolTable: SymbolTable,
) {
  constructor(
    lines: List<String>,
    packageName: String,
  ) : this(lines.joinToString(separator = "\n"), packageName)

  constructor(
    lines: String,
    packageName: String,
  ) : this(SymbolIo.readFromAaptNoValues(lines.byteInputStream().bufferedReader(), "R.txt", packageName))

  override fun toString(): String {
    val writer = StringWriter()
    writer.use { SymbolIo.writeForAar(symbolTable, it) }
    return writer.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (other !is RTxt) return false
    return symbolTable.symbols == other.symbolTable.symbols &&
      symbolTable.tablePackage == other.symbolTable.tablePackage
  }

  override fun hashCode(): Int {
    var result = 1
    result = 31 * result + symbolTable.symbols.hashCode()
    result = 31 * result + symbolTable.tablePackage.hashCode()
    return result
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

      val symbolTable = SymbolIo.readFromAaptNoValues(Files.newBufferedReader(path), path.toString(), packageName)
      return RTxt(symbolTable)
    }
  }
}
