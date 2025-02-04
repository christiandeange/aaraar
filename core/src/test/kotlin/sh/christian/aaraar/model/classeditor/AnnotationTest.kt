package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.ArrayValue
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.EnumValue
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.StringValue
import sh.christian.aaraar.utils.annotationsJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class AnnotationTest {
  @Test
  fun `annotation names`() {
    withClasspath(annotationsJarPath.loadJar()) { cp ->
      val annotations = cp["com.example.RegExp"].annotations

      annotations.map { it.qualifiedName } shouldContainExactly listOf(
        "java.lang.annotation.Documented",
        "java.lang.annotation.Retention",
        "java.lang.annotation.Target",
        "org.intellij.lang.annotations.Language",
      )

      annotations.map { it.packageName } shouldContainExactly listOf(
        "java.lang.annotation",
        "java.lang.annotation",
        "java.lang.annotation",
        "org.intellij.lang.annotations",
      )

      annotations.map { it.simpleName } shouldContainExactly listOf(
        "Documented",
        "Retention",
        "Target",
        "Language",
      )
    }
  }

  @Test
  fun `annotation visibility`() {
    withClasspath(annotationsJarPath.loadJar()) { cp ->
      val annotations = cp["com.example.RegExp"]
        .annotations.associate { it.simpleName to it.isVisible }

      annotations shouldHaveSize 4
      annotations["Documented"] shouldBe true
      annotations["Retention"] shouldBe true
      annotations["Target"] shouldBe true
      annotations["Language"] shouldBe false
    }
  }

  @Test
  fun `annotation parameters`() {
    withClasspath(annotationsJarPath.loadJar()) { cp ->
      val annotations = cp["com.example.RegExp"]
        .annotations
        .associate { it.simpleName to it.parameters }

      annotations shouldHaveSize 4
      annotations["Documented"]!!.shouldBeEmpty()
      annotations["Retention"]!! shouldContainExactly mapOf(
        "value" to EnumValue(cp["java.lang.annotation.RetentionPolicy"], "CLASS"),
      )
      annotations["Target"]!! shouldContainExactly mapOf(
        "value" to ArrayValue(
          listOf(
            EnumValue(cp["java.lang.annotation.ElementType"], "METHOD"),
            EnumValue(cp["java.lang.annotation.ElementType"], "FIELD"),
            EnumValue(cp["java.lang.annotation.ElementType"], "PARAMETER"),
            EnumValue(cp["java.lang.annotation.ElementType"], "LOCAL_VARIABLE"),
            EnumValue(cp["java.lang.annotation.ElementType"], "ANNOTATION_TYPE"),
          )
        ),
      )
      annotations["Language"]!! shouldContainExactly mapOf(
        "value" to StringValue("RegExp"),
      )
    }
  }

  @Test
  fun `toString with annotation instance`() {
    withClasspath(annotationsJarPath.loadJar()) { cp ->
      val annotations = cp["com.example.RegExp"].annotations.associateBy { it.simpleName }

      annotations["Documented"].toString() shouldBe "@java.lang.annotation.Documented"
      annotations["Retention"].toString() shouldBe "@java.lang.annotation.Retention([1 parameter])"
      annotations["Target"].toString() shouldBe "@java.lang.annotation.Target([1 parameter])"
      annotations["Language"].toString() shouldBe "@org.intellij.lang.annotations.Language([1 parameter])"
    }
  }
}
