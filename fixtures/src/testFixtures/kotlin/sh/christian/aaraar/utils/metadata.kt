package sh.christian.aaraar.utils

import io.kotest.matchers.shouldBe
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.JvmMetadataVersion
import kotlinx.metadata.jvm.KotlinClassMetadata

infix fun KmClass.shouldBe(other: KmClass) {
  val class1 = KotlinClassMetadata.Class(
    kmClass = this,
    version = JvmMetadataVersion.LATEST_STABLE_SUPPORTED,
    flags = 0,
  )

  val class2 = KotlinClassMetadata.Class(
    kmClass = other,
    version = JvmMetadataVersion.LATEST_STABLE_SUPPORTED,
    flags = 0,
  )

  class1.write() shouldBe class2.write()
}
