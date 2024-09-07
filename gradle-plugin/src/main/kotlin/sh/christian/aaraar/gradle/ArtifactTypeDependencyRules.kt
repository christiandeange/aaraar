package sh.christian.aaraar.gradle

import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.MultipleCandidatesDetails

internal const val MERGEABLE_ARTIFACT_TYPE = "mergeable-artifact"
private val MERGEABLE_ARTIFACT_TYPES = setOf("jar", "android-lint-local-aar", "aar")

internal class ArtifactTypeCompatibilityDependencyRule : AttributeCompatibilityRule<String> {
  override fun execute(t: CompatibilityCheckDetails<String>) {
    if (t.consumerValue == MERGEABLE_ARTIFACT_TYPE) {
      if (t.producerValue in MERGEABLE_ARTIFACT_TYPES) {
        t.compatible()
      } else {
        t.incompatible()
      }
    }
  }
}

internal class ArtifactTypeDisambiguationDependencyRule : AttributeDisambiguationRule<String> {
  override fun execute(t: MultipleCandidatesDetails<String>) {
    if (t.consumerValue == MERGEABLE_ARTIFACT_TYPE) {
      t.candidateValues
        .maxByOrNull { MERGEABLE_ARTIFACT_TYPES.indexOf(it) }
        ?.let { t.closestMatch(it) }
    }
  }
}
