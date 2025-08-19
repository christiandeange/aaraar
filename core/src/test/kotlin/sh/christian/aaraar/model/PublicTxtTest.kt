package sh.christian.aaraar.model

import com.android.ide.common.symbols.Symbol.Companion.createSymbol
import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import sh.christian.aaraar.utils.shouldHaveContents
import sh.christian.aaraar.utils.withFile
import kotlin.test.Test

class PublicTxtTest {

  @Test
  fun `empty symbol table does not write public txt file`() = withFile {
    val publicTxt = PublicTxt(symbolTable = SymbolTable.builder().build())

    publicTxt.writeTo(filePath)
    filePath.shouldNotExist()
  }

  @Test
  fun `symbol table with one symbol writes public txt file`() = withFile {
    val symbolTable = SymbolTable.builder()
      .add(createSymbol(ResourceType.STRING, "app_name", value = 100))
      .build()

    val publicTxt = PublicTxt(symbolTable = symbolTable)

    publicTxt.writeTo(filePath)
    filePath.shouldExist()
    filePath shouldHaveContents """
      string app_name
    """
  }

  @Test
  fun `symbol table with multiple symbol writes public txt file`() = withFile {
    val symbolTable = SymbolTable.builder()
      .add(createSymbol(ResourceType.STRING, "app_name", value = 100))
      .add(createSymbol(ResourceType.STRING, "activity_name", value = 101))
      .add(createSymbol(ResourceType.INTEGER, "anim_duration", value = 102))
      .add(createSymbol(ResourceType.FONT, "inter", value = 103))
      .add(createSymbol(ResourceType.FONT, "proxima_nova", value = 104))
      .add(createSymbol(ResourceType.BOOL, "is_tablet", value = 105))
      .build()

    val publicTxt = PublicTxt(symbolTable = symbolTable)

    publicTxt.writeTo(filePath)
    filePath.shouldExist()
    filePath shouldHaveContents """
      bool is_tablet
      font inter
      font proxima_nova
      integer anim_duration
      string activity_name
      string app_name
    """
  }
}
