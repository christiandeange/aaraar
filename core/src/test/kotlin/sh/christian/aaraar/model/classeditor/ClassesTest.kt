package sh.christian.aaraar.model.classeditor

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.StringValue
import sh.christian.aaraar.model.classeditor.Modifier.ABSTRACT
import sh.christian.aaraar.model.classeditor.Modifier.ANNOTATION
import sh.christian.aaraar.model.classeditor.Modifier.INTERFACE
import sh.christian.aaraar.model.classeditor.Modifier.PRIVATE
import sh.christian.aaraar.model.classeditor.Modifier.PROTECTED
import sh.christian.aaraar.model.classeditor.Modifier.PUBLIC
import sh.christian.aaraar.model.classeditor.types.stringType
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shouldBeDecompiledTo
import sh.christian.aaraar.utils.withClasspath
import kotlin.test.Test

class ClassesTest {

  @Test
  fun `default class`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")

    person.qualifiedName shouldBe "com.example.Person"
    person.simpleName shouldBe "Person"
    person.packageName shouldBe "com.example"

    person shouldBeDecompiledTo """
      package com.example;

      public class Person {
      }
    """
  }

  @Test
  fun `class with new name`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    person.qualifiedName shouldBe "com.example.Person"

    person.qualifiedName = "com.example.Animal"
    person.qualifiedName shouldBe "com.example.Animal"

    person.simpleName = "Plant"
    person.simpleName shouldBe "Plant"

    person.packageName = "com.organism"
    person.packageName shouldBe "com.organism"

    person.qualifiedName shouldBe "com.organism.Plant"

    person shouldBeDecompiledTo """
      package com.organism;

      public class Plant {
      }
    """
  }

  @Test
  fun `class with modifiers`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person") {
      modifiers = setOf(PROTECTED, ABSTRACT)
    }

    person.modifiers shouldContainExactly setOf(PROTECTED, ABSTRACT)

    person shouldBeDecompiledTo """
      package com.example;

      protected abstract class Person {
          public Person() {
          }
      }
    """
  }

  @Test
  fun `class with supertypes`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person") {
      val animal = cp.getOrCreate("com.example.Animal")
      val hasAge = cp.getOrCreate("com.example.HasAge")
      val hasName = cp.getOrCreate("com.example.HasName")

      superclass = animal
      interfaces = listOf(hasAge, hasName)

      addConstructor()
    }

    person.superclass?.qualifiedName shouldBe "com.example.Animal"
    person.interfaces.should { interfaces ->
      interfaces shouldHaveSize 2
      interfaces[0].qualifiedName shouldBe "com.example.HasAge"
      interfaces[1].qualifiedName shouldBe "com.example.HasName"
    }

    person shouldBeDecompiledTo """
      package com.example;

      public class Person extends Animal implements HasAge, HasName {
          public Person() {
          }
      }
    """
  }

  @Test
  fun `annotation class`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person") {
      modifiers = setOf(PUBLIC, ABSTRACT, ANNOTATION, INTERFACE)
      superclass = cp["java.lang.annotation.Annotation"]
    }

    person shouldBeDecompiledTo """
      package com.example;

      public @interface Person {
      }
    """
  }

  @Test
  fun `class with annotations`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person") {
      annotations += annotationInstance(cp["java.lang.Deprecated"]) {
        addValue("since", StringValue("11"))
      }
    }

    person.annotations.should { annotations ->
      annotations shouldHaveSingleElement person.annotationInstance(cp["java.lang.Deprecated"]) {
        addValue("since", StringValue("11"))
      }
    }

    person shouldBeDecompiledTo """
      package com.example;

      @Deprecated(
          since = "11"
      )
      public class Person {
      }
    """
  }

  @Test
  fun `class with members`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    person.addConstructor {
      val nonNull = cp.getOrCreate("javax.annotation.NonNull")

      setParameters(
        NewParameter(
          name = "name",
          type = cp.stringType,
          annotations = listOf(person.annotationInstance(nonNull)),
        )
      )
    }
    person.addField("name", cp.stringType)
    person.addMethod("getName") {
      returnType = cp.stringType
    }

    person.constructors shouldHaveSize 1
    person.fields shouldHaveSize 1
    person.methods shouldHaveSize 1

    person.getField("name").shouldNotBeNull()
    person.getMethod("getName").shouldNotBeNull()

    person shouldBeDecompiledTo """
      package com.example;

      import javax.annotation.NonNull;

      public class Person {
          public String name;

          public Person(@NonNull String name) {
          }

          public String getName() {
          }
      }
    """
  }

  @Test
  fun `strip code for api jar`() {
    withClasspath(fooJarPath.loadJar()) { cp ->
      cp["com.example.Foo"] shouldBeDecompiledTo """
        package com.example;
  
        public class Foo {
            void printHello() {
                System.out.println("Hello, world!");
            }
        }
      """
    }

    withClasspath(fooJarPath.loadJar()) { cp ->
      cp.removeMethodBodies()
      cp["com.example.Foo"] shouldBeDecompiledTo """
        package com.example;

        public class Foo {
            public Foo() {
            }

            void printHello() {
            }
        }
      """
    }
  }

  @Test
  fun `class version`() {
    withClasspath(fooJarPath.loadJar()) { cp ->
      val foo = cp["com.example.Foo"]
      foo.classMajorVersion shouldBe 55
      foo.classMinorVersion shouldBe 0
    }
  }

  @Test
  fun `class with new class version`() {
    withClasspath(fooJarPath.loadJar()) { cp ->
      val foo = cp["com.example.Foo"]
      foo.classMajorVersion = 62
      foo.classMinorVersion = 101

      foo.classMajorVersion shouldBe 62
      foo.classMinorVersion shouldBe 101
    }
  }

  @Test
  fun `class with private members`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    person.addField("name", cp.stringType) {
      modifiers = setOf(PRIVATE)
    }
    person.addMethod("setName") {
      setParameters(NewParameter("name", cp.stringType))
    }

    person shouldBeDecompiledTo """
      package com.example;

      public class Person {
          private String name;

          public void setName(String name) {
          }
      }
    """
  }

  @Test
  fun `class with private members stripped`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    person.addField("name", cp.stringType) {
      modifiers = setOf(PRIVATE)
    }
    person.addMethod("setName") {
      setParameters(NewParameter("name", cp.stringType))
    }

    cp.removePrivateMembers()
    person shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public void setName(String name) {
          }
      }
    """
  }

  @Test
  fun `toString prints name`() {
    withClasspath(fooJarPath.loadJar()) { cp ->
      cp["com.example.Foo"].toString() shouldBe "com.example.Foo"
    }
  }
}
