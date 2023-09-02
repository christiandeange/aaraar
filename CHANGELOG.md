Change Log
==========

## Version 0.0.10

_2023-09-02_

* New: Generate Dokka documentation.
* New: Hosted setup guide at https://christiandeange.github.io/aaraar.
* New: Support for Java projects!
  * The same `sh.christian.aaraar` plugin can be applied to Java or Kotlin libraries that produce a `jar` file instead.
  * Configuration is also done through the same `aaraar` Gradle extension.
  * See the new [publishing docs](https://christiandeange.github.io/aaraar/publishing-jar) for details on how to publish embedded `jar` files.

## Version 0.0.9

_2023-08-10_

* New: Support for merging navigation.json file.
* New: Preserve aar-metadata.properties file.
* Fix: Continue using APIs that are compatible with AGP7 and Java 11.

## Version 0.0.8

_2023-06-01_

* New: Run aaraar packaging during assemble pipeline.
  * Developers can choose which variants enable aaraar packaging using the existing `aaraar` extension:
    ```
    aaraar {
      isEnabledForVariant { it.name == "release" }
    }
    ```
* Update: Support for consuming Gradle Module Metadata.
* Update: Support for Android Gradle Plugin 8.

## Version 0.0.7

_2023-03-29_

* Fix: Add missing "usage" attribute to configuration.

## Version 0.0.6

_2023-03-13_

* Fix: Capture missing API element modules.

## Version 0.0.5

_2023-02-28_

* New: Respect `packagingOptions.resources.excludes` rules configured for project.
  * If any file globs are specified to be excluded, those files should always be deleted from the packaged aar.

## Version 0.0.4

_2023-02-10_

* Update: Flatten and merge all library dependency jars into single `classes.jar`.
* Update: Make core data models have internal constructors but public property access.
* Fix: Add Usage attribute to aar publishing configuration.

## Version 0.0.3

_2023-02-03_

* Fix: Support for compiling resources with custom xmlns namespaces.

## Version 0.0.2

_2023-01-29_

* New: Updated release pipeline and CI builds.
* Fix: Create extension as soon as plugin is applied.

## Version 0.0.1

_2023-01-24_

* Initial release.
