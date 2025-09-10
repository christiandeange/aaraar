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
