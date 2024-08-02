Change Log
==========

## Version 0.0.16

_2024-08-02_

* Update: Modularize Gradle packaging code to allow for reuse in other projects.
* Fix: Avoid exception when combining packaging option rules for Android projects.

## Version 0.0.15

_2024-06-24_

* New: Support custom shading strategies in core utilities module.
* New: Support for the entire `packaging` configuration when merging and packaging Android projects.
* Update: Changes to `api.jar` generation:
  * New: Support for reading and writing the class file version.
  * Note: Adding/modifying enum classes is not supported, and will be ignored.

## Version 0.0.14

_2024-03-10_

* Update: Shading configuration syntax now supports applying one rule to multiple scopes.
* Update: Allows disabling `api.jar` generation by providing an `ApiJarProcessor.Factory.None` by default.
* Update: `api.jar` generation now allows reading and writing annotations that are not visible via reflection.

## Version 0.0.13

_2024-02-27_

* New: Support for creating a custom `api.jar` in an AAR file.
    * This optional jar is used by the IDE as the autocomplete source, as well as what consumers compile against.
    * Does not affect which classes and class members are executable at runtime.
    * Provides an integration point for complete customization to add/remove classes, rename methods, and more.
    * See [updated packaging guide](https://aaraar.christian.sh/packaging) for info on configuration.

## Version 0.0.12

_2024-02-07_

* New: Support for limiting shading rules to a particular scope.
    * See [updated shading guide](https://aaraar.christian.sh/shading) for info on configuration and syntax.
* New: Added `addPrefix()` shorthand method for shading to add a prefix to each class package.

## Version 0.0.11

_2024-02-01_

* Update: The `keepMetaFiles` configuration option now defaults to `true`.
* Fix: Resolve issue opening/creating archive files in Windows.

## Version 0.0.10

_2023-09-02_

* New: Generate Dokka documentation.
* New: Hosted setup guide at https://aaraar.christian.sh.
* New: Support for Java projects!
    * The same `sh.christian.aaraar` plugin can be applied to Java or Kotlin libraries that produce a `jar` file instead.
    * Configuration is also done through the same `aaraar` Gradle extension.
    * See the new [publishing docs](https://aaraar.christian.sh/publishing-jar) for details on how to publish embedded `jar` files.

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
