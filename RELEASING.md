# Production Releases

1. Checkout `origin/main`.
2. Remove the `-SNAPSHOT` suffix from the version in `gradle.properties`.
3. Bump the versions in `sample-lib/library/build.gradle` and `README.md`.
4. Commit the changes and create a tag:
   ```shell
   git commit -am "Releasing v0.1.0"
   git tag v0.1.0
   ```
5. Bump the version in `gradle.properties` and add the `-SNAPSHOT` suffix.
6. Commit the change:
   ```shell
   git commit -am "Prepare next development cycle."
   ```
7. Push the commits. A new release will automatically be published on Sonatype.
   ```shell
   git push && git push --tags
   ```
8. Create a new release on GitHub for the newly-minted tag.
