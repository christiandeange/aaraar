package sh.christian.aaraar.model

import io.kotest.matchers.maps.shouldContainKeys
import org.junit.jupiter.api.Test
import sh.christian.aaraar.utils.externalLibsPath
import sh.christian.aaraar.utils.shouldContainExactly

class LibsTest {

  @Test
  fun `identifies jar files`() {
    val libs = Libs.from(externalLibsPath)
    libs.files.shouldContainExactly(
      "external.jar",
      "foo.jar",
      "license.txt",
    )

    libs.jars().shouldContainKeys(
      "external.jar",
      "foo.jar",
    )
  }
}
