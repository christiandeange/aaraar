package sh.christian.aaraar.model.lib

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import sh.christian.aaraar.model.lib.data.StringTable
import sh.christian.aaraar.model.lib.data.SymbolTable
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.nativelibJarPath
import java.nio.file.Paths
import kotlin.io.path.writeBytes
import kotlin.test.Test

class NativeLibraryTest {

  private val tinyMethods = setOf(
    "tiny_add",
    "hello_world",
    "print_hello_world",
  )

  @Test
  fun `reading tiny native fixture`() {
    val inBytes = nativelibJarPath.loadJar()["libtiny.so"]!!
    val lib = NativeLibrary.from(inBytes)
    val outBytes = lib.bytes()

    Paths.get("/Users/chr/Desktop/libtiny-new.so").writeBytes(outBytes)

    inBytes.asList() shouldBe outBytes.asList()
  }

  @Test
  fun `reading and writing tiny native fixture has ELF header`() {
    val inBytes = nativelibJarPath.loadJar()["libtiny.so"]!!
    val lib = NativeLibrary.from(inBytes)
    val outBytes = lib.bytes()

    outBytes[0] shouldBe 0x7F.toByte()
    outBytes[1] shouldBe 'E'.code.toByte()
    outBytes[2] shouldBe 'L'.code.toByte()
    outBytes[3] shouldBe 'F'.code.toByte()
  }

  @Test
  fun `reading tiny native fixture has method names in string table section`() {
    val inBytes = nativelibJarPath.loadJar()["libtiny.so"]!!
    val lib = NativeLibrary.from(inBytes)

    val strings = lib.sections.firstOrNull { it.name == ".strtab" }
    strings.shouldNotBeNull()
    strings.data.shouldBeInstanceOf<StringTable>()
    strings.data.strings shouldContainAll tinyMethods
  }

  @Test
  fun `all methods are in dynsym and symtab sections`() {
    val inBytes = nativelibJarPath.loadJar()["libtiny.so"]!!
    val lib = NativeLibrary.from(inBytes)

    lib.sections.forEach { section ->
      if (section.name == ".dynsym" || section.name == ".symtab") {
        section.data.shouldBeInstanceOf<SymbolTable>()
        withClue("Section '${section.name}': ") {
          section.data.symbols.map { it.name } shouldContainAll tinyMethods
        }
      }
    }
  }
}
