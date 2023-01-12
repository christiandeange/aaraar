package sh.christian.aaraar

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject

abstract class AarAarExtension
@Inject constructor(
  objects: ObjectFactory,
) {
  val prefix: Property<String> = objects.property<String>().convention("")
  val packagesToShade: SetProperty<String> = objects.setProperty<String>().convention(emptySet())
  val packagesToRemove: SetProperty<String> = objects.setProperty<String>().convention(emptySet())
}
