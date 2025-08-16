package sh.christian.aaraar.packaging

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import sh.christian.aaraar.Environment
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.packaging.ShadeConfigurationScope.DependencyScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.ProjectScope
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.foo2JarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.shouldContainExactly
import kotlin.test.Test

class PackagerTest {

  private val defaultEnvironment = Environment(androidAaptIgnore = "", keepClassesMetaFiles = true)
  private val defaultPackagingEnvironment = PackagingEnvironment.None
  private val defaultShadeEnvironment = ShadeEnvironment.None
  private val defaultLogger = PackagerLogger { println(it) }

  private val defaultPackager: Packager = createPackager()

  private val fooScope: ShadeConfigurationScope = ProjectScope(":foo")
  private val animalScope: ShadeConfigurationScope = ProjectScope(":animal")
  private val externalScope: ShadeConfigurationScope = DependencyScope("com.example", "animal", "1.0.0")

  @Test
  fun `nothing to merge`() {
    val input = defaultPackager.prepareInputArchive(animalJarPath, animalScope)
    val output = defaultPackager.mergeArchives(input, emptyList())

    output.classes.archive.shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
    )
  }

  @Test
  fun `simple merge with classes`() {
    val input = defaultPackager.prepareInputArchive(fooJarPath, fooScope)
    val dependency = defaultPackager.prepareDependencyArchive(animalJarPath, animalScope)
    val output = defaultPackager.mergeArchives(input, listOf(dependency))

    output.classes.archive.shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
      "com/example/Foo.class",
    )
  }

  @Test
  fun `merge with self is redundant`() {
    val input = defaultPackager.prepareInputArchive(fooJarPath, fooScope)
    val output = defaultPackager.mergeArchives(input, listOf(input))

    output.classes.archive.shouldContainExactly(
      "com/example/Foo.class",
    )
  }

  @Test
  fun `merge with classes with conflicting class files fails`() {
    val input = defaultPackager.prepareInputArchive(fooJarPath, fooScope)
    val dependency = defaultPackager.prepareDependencyArchive(foo2JarPath, fooScope)

    shouldThrow<IllegalStateException> {
      defaultPackager.mergeArchives(input, listOf(dependency))
    }
  }

  @Test
  fun `shade with no rules does nothing`() {
    val packager = createPackager(
      shadeEnvironment = ShadeEnvironment.None,
    )

    val output = packager.mergeArchives(
      inputArchive = packager.prepareInputArchive(fooJarPath, fooScope),
      dependencyArchives = listOf(packager.prepareDependencyArchive(animalJarPath, animalScope))
    )

    output.classes.archive.shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
      "com/example/Foo.class",
    )
  }

  @Test
  fun `shade all by package name`() {
    val packager = createPackager(
      shadeEnvironment = ShadeEnvironment(
        rules = listOf(
          ShadeConfigurationRule(
            scope = ShadeConfigurationScope.All,
            configuration = shadeConfiguration(
              classRenames = mapOf("com.example.**" to "com.biganimalcorp.@1"),
            ),
          )
        ),
      ),
    )

    val output = packager.mergeArchives(
      inputArchive = packager.prepareInputArchive(fooJarPath, fooScope),
      dependencyArchives = listOf(packager.prepareDependencyArchive(animalJarPath, animalScope))
    )

    output.classes.archive.shouldContainExactly(
      "com/biganimalcorp/Animal.class",
      "com/biganimalcorp/Cat.class",
      "com/biganimalcorp/Dog.class",
      "com/biganimalcorp/Foo.class",
    )
  }

  @Test
  fun `shade single project scope`() {
    val packager = createPackager(
      shadeEnvironment = ShadeEnvironment(
        rules = listOf(
          ShadeConfigurationRule(
            scope = animalScope,
            configuration = shadeConfiguration(
              classRenames = mapOf("com.example.**" to "com.biganimalcorp.@1"),
            ),
          )
        ),
      ),
    )

    val output = packager.mergeArchives(
      inputArchive = packager.prepareInputArchive(fooJarPath, fooScope),
      dependencyArchives = listOf(packager.prepareDependencyArchive(animalJarPath, animalScope))
    )

    output.classes.archive.shouldContainExactly(
      "com/biganimalcorp/Animal.class",
      "com/biganimalcorp/Cat.class",
      "com/biganimalcorp/Dog.class",
      "com/example/Foo.class",
    )
  }

  @Test
  fun `shade multiple scopes`() {
    val packager = createPackager(
      shadeEnvironment = ShadeEnvironment(
        rules = listOf(
          ShadeConfigurationRule(
            scope = fooScope and animalScope,
            configuration = shadeConfiguration(
              classRenames = mapOf("com.example.**" to "com.fooanimals.@1"),
            ),
          )
        ),
      ),
    )

    val output = packager.mergeArchives(
      inputArchive = packager.prepareInputArchive(fooJarPath, fooScope),
      dependencyArchives = listOf(packager.prepareDependencyArchive(animalJarPath, animalScope))
    )

    output.classes.archive.shouldContainExactly(
      "com/fooanimals/Animal.class",
      "com/fooanimals/Cat.class",
      "com/fooanimals/Dog.class",
      "com/fooanimals/Foo.class",
    )
  }

  @Test
  fun `shade external scope against all matching dependency scopes`() {
    val dependencyScopes = listOf(
      DependencyScope("com.example"),
      DependencyScope("com.example", "animal"),
      DependencyScope("com.example", "animal", "1.0.0"),
    )

    dependencyScopes.forEach { dependencyScope ->
      withClue("Dependency scope: $dependencyScope") {
        val packager = createPackager(
          shadeEnvironment = ShadeEnvironment(
            rules = listOf(
              ShadeConfigurationRule(
                scope = dependencyScope,
                configuration = shadeConfiguration(
                  classDeletes = setOf("com.example.**"),
                ),
              )
            ),
          ),
        )

        val output = packager.mergeArchives(
          inputArchive = packager.prepareInputArchive(fooJarPath, fooScope),
          dependencyArchives = listOf(packager.prepareDependencyArchive(animalJarPath, externalScope))
        )

        output.classes.archive.shouldContainExactly(
          "com/example/Foo.class",
        )
      }
    }
  }

  private fun createPackager(
    environment: Environment = defaultEnvironment,
    packagingEnvironment: PackagingEnvironment = defaultPackagingEnvironment,
    shadeEnvironment: ShadeEnvironment = defaultShadeEnvironment,
    logger: PackagerLogger = defaultLogger,
  ): Packager {
    return Packager(
      environment = environment,
      packagingEnvironment = packagingEnvironment,
      shadeEnvironment = shadeEnvironment,
      logger = logger,
    )
  }

  private fun shadeConfiguration(
    classRenames: Map<String, String> = emptyMap(),
    classDeletes: Set<String> = emptySet(),
    resourceRenames: Map<String, String> = emptyMap(),
    resourceDeletes: Set<String> = emptySet(),
  ): ShadeConfiguration {
    return ShadeConfiguration(classRenames, classDeletes, resourceRenames, resourceDeletes)
  }
}
