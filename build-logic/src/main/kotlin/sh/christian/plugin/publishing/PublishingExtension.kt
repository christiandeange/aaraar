package sh.christian.plugin.publishing

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class PublishingExtension
@Inject constructor(
  objects: ObjectFactory,
) {
  val group = objects.property<String>()
  val artifact = objects.property<String>()
  val version = objects.property<String>()
}
