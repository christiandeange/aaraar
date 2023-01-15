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
  val classRenames: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())
  val classDeletes: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())

  fun rename(pattern: String, replacement: String) {
    classRenames.put(pattern, replacement)
  }

  fun delete(pattern: String) {
    classDeletes.add(pattern)
  }
}
