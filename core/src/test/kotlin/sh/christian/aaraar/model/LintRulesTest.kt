package sh.christian.aaraar.model

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.serviceJarPath
import kotlin.test.Test

class LintRulesTest {
  @Test
  fun `test equality`() {
    val lintRules1 = LintRules.from(serviceJarPath)
    val lintRules2 = LintRules.from(serviceJarPath)
    lintRules1 shouldBe lintRules2
  }
}
