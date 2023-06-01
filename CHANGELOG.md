Change Log
==========

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
