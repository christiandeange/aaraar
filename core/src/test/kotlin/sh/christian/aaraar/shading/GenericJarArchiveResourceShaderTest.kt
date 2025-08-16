package sh.christian.aaraar.shading

import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldNotHaveKey
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.serviceJarPath
import kotlin.test.Test

class GenericJarArchiveResourceShaderTest {
  @Test
  fun `rename resource file by resource name`() {
    val originalClasses = serviceJarPath.loadJar()
    originalClasses shouldHaveKey "com/example/tracklist.txt"

    val shadedClasses = originalClasses.shaded(resourceRenames = mapOf("com/example/**" to "music/@0"))
    shadedClasses shouldHaveKey "music/com/example/tracklist.txt"
  }

  @Test
  fun `rename resource file by resource name does not affect class files`() {
    val originalClasses = serviceJarPath.loadJar()
    originalClasses shouldHaveKey "com/example/CustomService.class"

    val shadedClasses = originalClasses.shaded(resourceRenames = mapOf("com/example/**" to "music/@0"))
    shadedClasses shouldHaveKey "com/example/CustomService.class"
    shadedClasses shouldNotHaveKey "music/com/example/CustomService.class"
  }

  @Test
  fun `rename kotlin module by resource name`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses shouldHaveKey "META-INF/fixtures_ktLibrary.kotlin_module"

    val shadedClasses = originalClasses.shaded(
      resourceRenames = mapOf("META-INF/*.kotlin_module" to "META-INF/old/@1.kotlin_module"),
    )
    shadedClasses shouldHaveKey "META-INF/old/fixtures_ktLibrary.kotlin_module"
  }

  @Test
  fun `delete by resource name`() {
    val originalClasses = serviceJarPath.loadJar()
    originalClasses shouldHaveKey "com/example/tracklist.txt"

    val shadedClasses = originalClasses.shaded(resourceDeletes = setOf("com/example/**"))
    shadedClasses shouldNotHaveKey "com/example/tracklist.txt"
  }
}
