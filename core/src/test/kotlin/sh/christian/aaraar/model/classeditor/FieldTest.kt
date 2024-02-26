package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.classeditor.Modifier.FINAL
import sh.christian.aaraar.model.classeditor.Modifier.PROTECTED
import sh.christian.aaraar.model.classeditor.types.intType
import sh.christian.aaraar.model.classeditor.types.longType
import sh.christian.aaraar.utils.shouldBeDecompiledTo
import kotlin.test.Test

class FieldTest {

  @Test
  fun `default field`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addField("age", cp.intType)
    }

    foo.getField("age")!!.name shouldBe "age"

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public int age;
      }
    """
  }

  @Test
  fun `field with modifiers`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addField("age", cp.intType) {
        modifiers = setOf(FINAL, PROTECTED)
      }
    }

    foo.getField("age")!!.modifiers shouldContainOnly setOf(FINAL, PROTECTED)

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          protected final int age;
      }
    """
  }

  @Test
  fun `field with annotation`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addField("age", cp.intType) {
        annotations = listOf(annotationInstance(cp["java.lang.Deprecated"]))
      }
    }

    foo.getField("age")!!.annotations shouldHaveSingleElement {
      it.qualifiedName == "java.lang.Deprecated"
    }

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          @Deprecated
          public int age;
      }
    """
  }

  @Test
  fun `field with new name`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addField("age", cp.intType)
    }

    foo.getField("age")!!.name = "_age"
    foo.getField("age").shouldBeNull()
    foo.getField("_age")!!.name shouldBe "_age"

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public int _age;
      }
    """
  }

  @Test
  fun `field with new type`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addField("age", cp.intType)
    }

    foo.getField("age")!!.type = cp.longType
    foo.getField("age")!!.type shouldBe cp.longType

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public long age;
      }
    """
  }

  @Test
  fun `field with new annotation`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addField("age", cp.intType)
    }

    foo.getField("age")!!.annotations = listOf(foo.annotationInstance(cp["java.lang.Deprecated"]))
    foo.getField("age")!!.annotations shouldHaveSingleElement {
      it.qualifiedName == "java.lang.Deprecated"
    }

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          @Deprecated
          public int age;
      }
    """
  }
}
