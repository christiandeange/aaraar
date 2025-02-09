package sh.christian.aaraar.shading

import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldNotHaveKey
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import kotlin.test.Test

class GenericJarArchiveResourceShaderTest {
  @Test
  fun `delete by resource name`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses shouldHaveKey "META-INF/fixtures_ktLibrary.kotlin_module"

    val shadedClasses = originalClasses.shaded(resourceDeletes = setOf("**/*.kotlin_module"))
    shadedClasses shouldNotHaveKey "META-INF/fixtures_ktLibrary.kotlin_module"
  }

  @Test
  fun `cannot delete classes by resource name`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses shouldHaveKey "sh/christian/mylibrary/Foo.class"

    val shadedClasses = originalClasses.shaded(resourceDeletes = setOf("**/Foo.class"))
    shadedClasses shouldHaveKey "sh/christian/mylibrary/Foo.class"
  }
}
