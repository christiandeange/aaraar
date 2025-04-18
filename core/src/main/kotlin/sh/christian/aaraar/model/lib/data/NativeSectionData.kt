package sh.christian.aaraar.model.lib.data

import sh.christian.aaraar.model.lib.NativeLibrary
import sh.christian.aaraar.model.lib.NativeSection

sealed interface NativeSectionData {
  fun bytes(
    lib: NativeLibrary,
    section: NativeSection,
  ): ByteArray
}
