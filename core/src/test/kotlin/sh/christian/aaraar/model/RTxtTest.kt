package sh.christian.aaraar.model

import com.android.ide.common.symbols.Symbol.Companion.createSymbol
import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.shouldHaveContents
import sh.christian.aaraar.utils.withFile
import kotlin.test.Test

class RTxtTest {

  private val emptySymbolTable = SymbolTable.builder().build()

  private val smallSymbolTable = SymbolTable.builder()
    .add(createSymbol(ResourceType.STRING, "app_name", value = 100))
    .build()

  private val largeSymbolTable = SymbolTable.builder()
    .add(createSymbol(ResourceType.STRING, "app_name", value = 100))
    .add(createSymbol(ResourceType.STRING, "activity_name", value = 101))
    .add(createSymbol(ResourceType.INTEGER, "anim_duration", value = 102))
    .add(createSymbol(ResourceType.FONT, "inter", value = 103))
    .add(createSymbol(ResourceType.FONT, "proxima_nova", value = 104))
    .add(createSymbol(ResourceType.BOOL, "is_tablet", value = 105))
    .build()

  @Test
  fun `empty symbol table does not write R txt file`() = withFile {
    val rTxt = RTxt(symbolTable = emptySymbolTable)

    rTxt.writeTo(filePath)
    filePath.shouldNotExist()
  }

  @Test
  fun `symbol table with at least one symbol writes R txt file`() = withFile {
    val rTxt = RTxt(symbolTable = smallSymbolTable)

    rTxt.writeTo(filePath)
    filePath.shouldExist()
    filePath shouldHaveContents """
      int string app_name 0x64
    """
  }

  @Test
  fun `test toString`() {
    val rTxt = RTxt(symbolTable = largeSymbolTable)
    rTxt.toString() shouldBe """
      int bool is_tablet 0x69
      int font inter 0x67
      int font proxima_nova 0x68
      int integer anim_duration 0x66
      int string activity_name 0x65
      int string app_name 0x64

    """.trimIndent()
  }

  @Test
  fun `test equality`() {
    val rTxt1 = RTxt(symbolTable = largeSymbolTable)
    val rTxt2 = RTxt(symbolTable = largeSymbolTable)
    rTxt1 shouldBe rTxt2
  }
}
