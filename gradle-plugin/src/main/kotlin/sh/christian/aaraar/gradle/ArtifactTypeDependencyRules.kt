package sh.christian.aaraar.gradle

import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.AttributeDisambiguationRule
import org.gradle.api.attributes.CompatibilityCheckDetails
import org.gradle.api.attributes.MultipleCandidatesDetails

internal const val MERGEABLE_ARTIFACT_TYPE = "mergeable-artifact"
internal const val MERGED_ARTIFACT_TYPE = "merged-artifact"

private val MERGEABLE_TYPES = setOf(MERGED_ARTIFACT_TYPE, "aar", "android-lint-local-aar", "jar")

internal class ArtifactTypeCompatibilityDependencyRule : AttributeCompatibilityRule<String> {
  override fun execute(t: CompatibilityCheckDetails<String>) {
    if (t.consumerValue == MERGEABLE_ARTIFACT_TYPE) {
      if (t.producerValue in MERGEABLE_TYPES) {
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
      MERGEABLE_TYPES.firstOrNull { it in t.candidateValues }?.let { preferredType ->
        t.closestMatch(preferredType)
      }
    }
  }
}
