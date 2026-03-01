package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.NativeLibrary
import sh.christian.aaraar.model.lib.NativeSection
import sh.christian.aaraar.model.lib.Value
import sh.christian.aaraar.model.lib.elf.ElfSection

data class NobitsData(
  val size: Value,
) : NativeSectionData {
  override fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray = ByteArray(0)

  companion object {
    fun from(elfSection: ElfSection): NobitsData {
      return NobitsData(elfSection.sh_size)
    }
  }
}
