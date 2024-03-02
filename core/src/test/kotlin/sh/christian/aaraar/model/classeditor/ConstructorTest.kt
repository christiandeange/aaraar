package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.classeditor.Modifier.PROTECTED
import sh.christian.aaraar.model.classeditor.types.intType
import sh.christian.aaraar.model.classeditor.types.longType
import sh.christian.aaraar.utils.shouldBeDecompiledTo
import kotlin.test.Test

class ConstructorTest {
  @Test
  fun `default constructor`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addConstructor()
    }

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public Person() {
          }
      }
    """
  }

  @Test
  fun `constructor with modifiers`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addConstructor {
        modifiers = setOf(PROTECTED)
      }
    }

    foo.constructors shouldHaveSize 1
    foo.constructors.single().modifiers shouldHaveSingleElement PROTECTED

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          protected Person() {
          }
      }
    """
  }

  @Test
  fun `constructor with annotations`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addConstructor {
        annotations = listOf(annotationInstance(cp["java.lang.Deprecated"]))
      }
    }

    foo.constructors shouldHaveSize 1
    foo.constructors.single().annotations shouldHaveSingleElement foo.annotationInstance(cp["java.lang.Deprecated"])

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          @Deprecated
          public Person() {
          }
      }
    """
  }

  @Test
  fun `constructor with parameters`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addConstructor {
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType),
        )
      }
    }

    foo.constructors shouldHaveSize 1
    foo.constructors.single().parameters.should { parameters ->
      parameters shouldHaveSize 2
      parameters[0].should { param ->
        param.name shouldBe "age"
        param.type shouldBe cp.intType
        param.annotations.shouldBeEmpty()
      }
      parameters[1].should { param ->
        param.name shouldBe "birthYear"
        param.type shouldBe cp.longType
        param.annotations.shouldBeEmpty()
      }
    }

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public Person(int age, long birthYear) {
          }
      }
    """
  }

  @Test
  fun `constructor with updated parameters`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addConstructor {
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType),
        )

        setParameter(1, NewParameter("year", cp.intType))
      }
    }

    foo.constructors shouldHaveSize 1
    foo.constructors.single().parameters.should { parameters ->
      parameters shouldHaveSize 2
      parameters[0].should { param ->
        param.name shouldBe "age"
        param.type shouldBe cp.intType
        param.annotations.shouldBeEmpty()
      }
      parameters[1].should { param ->
        param.name shouldBe "year"
        param.type shouldBe cp.intType
        param.annotations.shouldBeEmpty()
      }
    }

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public Person(int age, int year) {
          }
      }
    """
  }

  @Test
  fun `constructor with annotated parameter`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addConstructor {
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType, listOf(annotationInstance(cp["java.lang.Deprecated"]))),
        )
      }
    }

    foo.constructors shouldHaveSize 1
    foo.constructors.single().parameters.should { parameters ->
      parameters shouldHaveSize 2
      parameters[0].should { param ->
        param.name shouldBe "age"
        param.type shouldBe cp.intType
        param.annotations.shouldBeEmpty()
      }
      parameters[1].should { param ->
        param.name shouldBe "birthYear"
        param.type shouldBe cp.longType
        param.annotations shouldHaveSingleElement foo.annotationInstance(cp["java.lang.Deprecated"])
      }
    }

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public Person(int age, @Deprecated long birthYear) {
          }
      }
    """
  }
}
