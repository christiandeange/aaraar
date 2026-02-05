package sh.christian.aaraar.utils

import io.kotest.matchers.shouldBe
import kotlin.metadata.KmClass
import kotlin.metadata.jvm.JvmMetadataVersion
import kotlin.metadata.jvm.KotlinClassMetadata

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
