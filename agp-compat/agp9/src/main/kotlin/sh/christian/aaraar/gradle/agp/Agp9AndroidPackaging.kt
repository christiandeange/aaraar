package sh.christian.aaraar.gradle.agp

import com.android.build.api.variant.JniLibsPackaging
import com.android.build.api.variant.Packaging
import com.android.build.api.variant.ResourcesPackaging
import org.gradle.api.provider.SetProperty

class Agp9AndroidPackaging(packaging: Packaging) : AndroidPackaging {
  override val jniLibs: AndroidPackaging.JniLibs = Agp8JniLibs(packaging.jniLibs)
  override val resources: AndroidPackaging.Resources = Agp8Resources(packaging.resources)

  private class Agp8JniLibs(jniLibs: JniLibsPackaging) : AndroidPackaging.JniLibs {
    override val pickFirsts: SetProperty<String> = jniLibs.pickFirsts
    override val excludes: SetProperty<String> = jniLibs.excludes
  }

  private class Agp8Resources(resources: ResourcesPackaging) : AndroidPackaging.Resources {
    override val pickFirsts: SetProperty<String> = resources.pickFirsts
    override val merges: SetProperty<String> = resources.merges
    override val excludes: SetProperty<String> = resources.excludes
  }
}
