package sh.christian.aaraar.shading

import sh.christian.aaraar.utils.forEntry
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.serviceJarPath
import kotlin.test.Test

class GenericJarArchiveServiceLoaderShaderTest {
  @Test
  fun `default service loader file`() {
    val originalClasses = serviceJarPath.loadJar()
    originalClasses.forEntry("META-INF/services/java.nio.file.spi.CustomService") shouldHaveFileContents """
      com.example.MyCustomService
      com.example.RealCustomService
    """
  }

  @Test
  fun `shading updates class references from service loader file`() {
    val shadedClasses = serviceJarPath.loadJar().shaded(
      classRenames = mapOf("com.example.MyCustomService" to "com.example.EmptyCustomService"),
    )
    shadedClasses.forEntry("META-INF/services/java.nio.file.spi.CustomService") shouldHaveFileContents """
      com.example.EmptyCustomService
      com.example.RealCustomService
    """
  }

  @Test
  fun `deleting some class references from service loader file removes them`() {
    val shadedClasses = serviceJarPath.loadJar().shaded(
      classDeletes = setOf("com.example.MyCustomService"),
    )
    shadedClasses.forEntry("META-INF/services/java.nio.file.spi.CustomService") shouldHaveFileContents """
      com.example.RealCustomService
    """
  }

  @Test
  fun `deleting all class references from service loader file removes the service loader file`() {
    val shadedClasses = serviceJarPath.loadJar().shaded(
      classDeletes = setOf("com.example.MyCustomService", "com.example.RealCustomService"),
    )
    shadedClasses.forEntry("META-INF/services/java.nio.file.spi.CustomService").shouldNotExist()
  }
}
