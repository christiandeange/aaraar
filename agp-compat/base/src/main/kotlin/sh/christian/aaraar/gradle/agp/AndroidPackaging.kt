package sh.christian.aaraar.gradle.agp

import org.gradle.api.provider.SetProperty

interface AndroidPackaging {
  val jniLibs: JniLibs
  val resources: Resources

  interface JniLibs {
    val excludes: SetProperty<String>
    val pickFirsts: SetProperty<String>
  }

  interface Resources {
    val excludes: SetProperty<String>
    val pickFirsts: SetProperty<String>
    val merges: SetProperty<String>
  }
}
