package sh.christian.aaraar

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject

abstract class AarAarExtension
@Inject constructor(
  objects: ObjectFactory,
) {
  val packagesToShade: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())
  val packagesToRemove: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())
}
