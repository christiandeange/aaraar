package sh.christian.aaraar.gradle.agp

import com.android.build.api.variant.JniLibsPackaging
import com.android.build.api.variant.Packaging
import com.android.build.api.variant.ResourcesPackaging
import org.gradle.api.provider.SetProperty

class Agp7AndroidPackaging(packaging: Packaging) : AndroidPackaging {
  override val jniLibs: AndroidPackaging.JniLibs = Agp7JniLibs(packaging.jniLibs)
  override val resources: AndroidPackaging.Resources = Agp7Resources(packaging.resources)

  private class Agp7JniLibs(jniLibs: JniLibsPackaging) : AndroidPackaging.JniLibs {
    override val pickFirsts: SetProperty<String> = jniLibs.pickFirsts
    override val excludes: SetProperty<String> = jniLibs.excludes
  }

  private class Agp7Resources(resources: ResourcesPackaging) : AndroidPackaging.Resources {
    override val pickFirsts: SetProperty<String> = resources.pickFirsts
    override val merges: SetProperty<String> = resources.merges
    override val excludes: SetProperty<String> = resources.excludes
  }
}
