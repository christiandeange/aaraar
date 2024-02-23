package sh.christian.aaraar.model.classeditor

import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.StringValue
import sh.christian.aaraar.model.classeditor.Modifier.ABSTRACT
import sh.christian.aaraar.model.classeditor.Modifier.ANNOTATION
import sh.christian.aaraar.model.classeditor.Modifier.INTERFACE
import sh.christian.aaraar.model.classeditor.Modifier.PRIVATE
import sh.christian.aaraar.model.classeditor.Modifier.PROTECTED
import sh.christian.aaraar.model.classeditor.types.booleanType
import sh.christian.aaraar.model.classeditor.types.intType
import sh.christian.aaraar.model.classeditor.types.stringType
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shouldBeDecompiledTo
import kotlin.test.Test

class ClassesTest {

  @Test
  fun `default class`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")

    person shouldBeDecompiledTo """
      package com.example;

      public class Person {
      }
    """
  }

  @Test
  fun `class with modifiers`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person") {
      modifiers = setOf(PROTECTED, ABSTRACT)
    }

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
      superclass = cp["com.example.Animal"]
      interfaces = listOf(cp["com.example.HasAge"], cp["com.example.HasName"])

      addConstructor()
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
      modifiers = setOf(ANNOTATION, INTERFACE)
    }

    person shouldBeDecompiledTo """
      package com.example;

      @interface Person {
          default Person() {
          }
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
  fun `class with fields`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    person.addField("firstName", cp.stringType)
    person.addField("lastName", cp.stringType)

    person shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public String firstName;
          public String lastName;
      }
    """.trimIndent()
  }

  @Test
  fun `class with members with parameters`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    person.addConstructor {
      setParameters(NewParameter("age", cp.intType))
    }
    person.addMethod("setAge") {
      setParameters(NewParameter("age", cp.intType))
    }
    person.addMethod("getAge") {
      returnType = cp.intType
    }
    person.addMethod("printAge")

    person shouldBeDecompiledTo """
      package com.example;

      public class Person {
          public Person(int age) {
          }

          public void setAge(int age) {
          }

          public int getAge() {
          }

          public void printAge() {
          }
      }
    """.trimIndent()
  }

  @Test
  fun `class with members with annotations`() = withClasspath { cp ->
    val person = cp.addClass("com.example.Person")
    val deprecated = person.annotationInstance(cp["java.lang.Deprecated"])
    val nonNull = person.annotationInstance(cp["javax.annotation.NonNull"])

    person.addConstructor {
      setParameters(NewParameter("gender", cp.stringType, listOf(nonNull)))
    }
    person.addConstructor {
      annotations = listOf(deprecated)
      setParameters(NewParameter("isMale", cp.booleanType))
    }
    person.addField("gender", cp.stringType)
    person.addField("isMale", cp.booleanType) {
      annotations = listOf(deprecated)
    }

    person shouldBeDecompiledTo """
      package com.example;

      import javax.annotation.NonNull;

      public class Person {
          public String gender;
          @Deprecated
          public boolean isMale;

          public Person(@NonNull String gender) {
          }

          @Deprecated
          public Person(boolean isMale) {
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
      """.trimIndent()
    }

    withClasspath(fooJarPath.loadJar()) { cp ->
      cp.removeMethodBodies()
      cp["com.example.Foo"] shouldBeDecompiledTo """
        package com.example;

        public class Foo {
            public Foo() {
            }

            public void printHello() {
            }
        }
      """.trimIndent()
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
    """.trimIndent()
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
    """.trimIndent()
  }

  private inline fun withClasspath(
    jar: GenericJarArchive = GenericJarArchive.NONE,
    crossinline block: (Classpath) -> Unit,
  ) = block(Classpath.from(jar))
}
