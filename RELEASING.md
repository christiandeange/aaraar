# Production Releases

1. Checkout `origin/main`.
2. Remove the `-SNAPSHOT` suffix from the version in `gradle.properties`.
3. Commit the changes and create a tag:
   ```shell
   git commit -am "Releasing v0.1.0"
   git tag v0.1.0
   ```
4. Manually (for now) run the tasks to create and release the staging repository on [Sonatype](https://s01.oss.sonatype.org).
   ```shell
   ./gradlew publish
   ./gradlew closeAndReleaseRepository
   ```

5. Bump the version in `gradle.properties` and add the `-SNAPSHOT` suffix.
6. Commit the change:
   ```shell
   git commit -am "Prepare next development cycle."
   ```
7. Push the commits.
   ```shell
   git push && git push --tags
   ```
8. Create a new release on GitHub for the newly-minted tag.
