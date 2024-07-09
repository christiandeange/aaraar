package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.IntegerValue
import sh.christian.aaraar.model.classeditor.Modifier.ANNOTATION
import sh.christian.aaraar.model.classeditor.Modifier.INTERFACE
import sh.christian.aaraar.model.classeditor.Modifier.PROTECTED
import sh.christian.aaraar.model.classeditor.types.intType
import sh.christian.aaraar.model.classeditor.types.longType
import sh.christian.aaraar.model.classeditor.types.stringType
import sh.christian.aaraar.utils.shouldBeDecompiledTo
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class MethodTest {

  @Test
  fun `default method`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("printAge")
    }

    foo.getMethod("printAge")!!.name shouldBe "printAge"

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public void printAge() {
          }
      }
    """
  }

  @Test
  fun `method with modifiers`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("printAge") {
        modifiers = setOf(PROTECTED)
      }
    }

    foo.getMethod("printAge")!!.modifiers shouldHaveSingleElement PROTECTED

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          protected void printAge() {
          }
      }
    """
  }

  @Test
  fun `method with annotations`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("printAge") {
        annotations = listOf(annotationInstance(cp["java.lang.Deprecated"]))
      }
    }

    foo.getMethod("printAge")!!.annotations shouldHaveSingleElement foo.annotationInstance(cp["java.lang.Deprecated"])

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          @Deprecated
          public void printAge() {
          }
      }
    """
  }

  @Test
  fun `method with return type`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("getAge") {
        returnType = cp.intType
      }
    }

    foo.getMethod("getAge")!!.returnType shouldBe cp.intType

    foo shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public int getAge() {
          }
      }
    """
  }

  @Test
  fun `method with default value`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      modifiers = setOf(ANNOTATION, INTERFACE)

      addMethod("getAge") {
        returnType = cp.intType
        defaultValue = IntegerValue(42)
      }
    }

    foo.getMethod("getAge")!!.defaultValue should {
      it.shouldBeInstanceOf<IntegerValue>()
      it.value shouldBe 42
    }

    foo shouldBeDecompiledTo """
      package com.example;

      @interface Person {
          int getAge() {
          }

          default Person() {
          }
      }
    """
  }

  @Test
  fun `method with parameters`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("setAge") {
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType),
        )
      }
    }

    foo.getMethod("setAge")!!.parameters.should { parameters ->
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
          public void setAge(int age, long birthYear) {
          }
      }
    """
  }

  @Test
  fun `method with updated parameters`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("setAge") {
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType),
        )

        setParameter(1, NewParameter("year", cp.intType))
      }
    }

    foo.getMethod("setAge")!!.parameters.should { parameters ->
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
          public void setAge(int age, int year) {
          }
      }
    """
  }

  @Test
  fun `constructor with annotated parameter`() = withClasspath { cp ->
    val foo = cp.addClass("com.example.Person") {
      addMethod("setAge") {
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType, listOf(annotationInstance(cp["java.lang.Deprecated"]))),
        )
      }
    }

    foo.getMethod("setAge")!!.parameters.should { parameters ->
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
          public void setAge(int age, @Deprecated long birthYear) {
          }
      }
    """
  }

  @Test
  fun `toString with simple method`() = withClasspath { cp ->
    cp.addClass("com.example.Person") {
      val constructor = addMethod("printDetails")
      constructor.toString() shouldBe "fun com.example.Person.printDetails()"
    }
  }

  @Test
  fun `toString with arguments and return type`() = withClasspath { cp ->
    cp.addClass("com.example.Person") {
      val constructor = addMethod("printDetails") {
        returnType = cp.stringType
        setParameters(
          NewParameter("age", cp.intType),
          NewParameter("birthYear", cp.longType, listOf(annotationInstance(cp["java.lang.Deprecated"]))),
        )
      }

      constructor.toString() shouldBe "fun com.example.Person.printDetails(age: Int, birthYear: Long): java.lang.String"
    }
  }
}
