package sh.christian.aaraar.gradle

import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.MultipleCandidatesDetails
import org.gradle.api.attributes.java.TargetJvmEnvironment

internal const val MERGEABLE_ENVIRONMENT = "mergeable-environment"
private val MERGEABLE_ENVIRONMENTS = setOf(TargetJvmEnvironment.STANDARD_JVM, TargetJvmEnvironment.ANDROID)

internal class TargetJvmEnvironmentCompatibilityDependencyRule : AttributeCompatibilityRule<TargetJvmEnvironment> {
  override fun execute(t: CompatibilityCheckDetails<TargetJvmEnvironment>) {
    if (t.consumerValue?.name == MERGEABLE_ENVIRONMENT) {
      if (t.producerValue?.name.orEmpty() in MERGEABLE_ENVIRONMENTS) {
        t.compatible()
      } else {
        t.incompatible()
      }
    }
  }
}

internal class TargetJvmEnvironmentDisambiguationDependencyRule : AttributeDisambiguationRule<TargetJvmEnvironment> {
  override fun execute(t: MultipleCandidatesDetails<TargetJvmEnvironment>) {
    if (t.consumerValue?.name == MERGEABLE_ARTIFACT_TYPE) {
      t.candidateValues
        .maxByOrNull { MERGEABLE_ENVIRONMENTS.indexOf(it.name) }
        ?.let { t.closestMatch(it) }
    }
  }
}
