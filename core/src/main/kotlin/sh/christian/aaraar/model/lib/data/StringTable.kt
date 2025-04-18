package sh.christian.aaraar.model.lib.data

import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import sh.christian.aaraar.model.lib.NativeLibrary
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.elf.ElfSection

data class StringTable(
  val strings: List<String>,
) : NativeSectionData {
  private val data: ByteArray by lazy {
    strings.joinToString(separator = "\u0000").encodeUtf8().toByteArray()
  }

  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray = data

  val dataAsString: String by lazy {
    data.toByteString().utf8()
  }

  fun stringAt(offset: Int): String {
    return dataAsString.substring(offset, dataAsString.indexOf('\u0000', offset))
  }

  fun offsetOf(string: String): Int {
    return dataAsString.indexOf(string + "\u0000").also { index ->
      require(index >= 0) {
        "'$string' not found in string table."
      }
    }
  }

  companion object {
    fun from(elfSection: ElfSection): StringTable {
      val strings = elfSection.data.data.toByteString().utf8().split('\u0000')
      return StringTable(strings)
    }
  }
}
