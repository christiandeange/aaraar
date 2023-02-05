package sh.christian.aaraar

import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import org.junit.jupiter.api.Test
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.utils.externalLibsPath

class LibsTest {

  @Test
  fun `identifies jar files`() {
    val libs = Libs.from(externalLibsPath)
    with(libs.files) {
      this shouldHaveSize 3
      this shouldHaveKey "external.jar"
      this shouldHaveKey "foo.jar"
      this shouldHaveKey "license.txt"
    }

    with(libs.jars()) {
      this shouldHaveSize 2
      this shouldHaveKey "external.jar"
      this shouldHaveKey "foo.jar"
    }
  }
}
