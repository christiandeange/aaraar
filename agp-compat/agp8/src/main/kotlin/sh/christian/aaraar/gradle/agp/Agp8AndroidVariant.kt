package sh.christian.aaraar.gradle.agp

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryVariant
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
internal class Agp8AndroidVariant(
  private val variant: LibraryVariant,
) : AndroidVariant {
  override val variantName: String = variant.name
  override val buildType: String? = variant.buildType
  override val compileConfiguration: Configuration = variant.compileConfiguration
  override val runtimeConfiguration: Configuration = variant.runtimeConfiguration

  override fun artifactFile(type: FileArtifactType): Provider<RegularFile> = when (type) {
    FileArtifactType.AAR -> variant.artifacts.get(SingleArtifact.AAR)
  }
}
