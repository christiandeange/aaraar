package sh.christian.aaraar.model

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the contents of the `public.txt` file.
 */
class PublicTxt
internal constructor(
  internal val symbolTable: SymbolTable,
) {
  constructor(
    lines: List<String>,
    packageName: String,
  ) : this(lines.joinToString(separator = "\n"), packageName)

  constructor(
    lines: String,
    packageName: String,
  ) : this(SymbolIo.readFromPublicTxtFile(lines.byteInputStream(), "public.txt", packageName))

  override fun toString(): String {
    return ResourceType.values()
      .flatMap { type ->
        symbolTable.getSymbolByResourceType(type)
      }
      .joinToString("\n") { symbol ->
        @Suppress("UsePropertyAccessSyntax")
        "${symbol.resourceType.getName()} ${symbol.canonicalName}"
      }
  }

  fun writeTo(path: Path) {
    if (symbolTable.symbols.isEmpty) {
      Files.deleteIfExists(path)
    } else {
      Files.writeString(path, toString())
    }
  }

  companion object {
    fun from(path: Path, packageName: String): PublicTxt {
      if (!Files.isRegularFile(path)) return PublicTxt(symbolTable = SymbolTable.builder().build())

      val symbolTable = SymbolIo.readFromPublicTxtFile(Files.newInputStream(path), path.toString(), packageName)
      return PublicTxt(symbolTable)
    }
  }
}
