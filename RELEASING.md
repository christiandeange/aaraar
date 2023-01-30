# Production Releases

1. Checkout `origin/main`.
2. Remove the `-SNAPSHOT` suffix from the version in `gradle.properties`.
3. Commit the changes and create a tag:
   ```shell
   git commit -am "Releasing v0.1.0"
   git tag v0.1.0
   ```
4. Bump the version in `gradle.properties` and add the `-SNAPSHOT` suffix.
5. Commit the change:
   ```shell
   git commit -am "Prepare next development cycle."
   ```
6. Push the commits. A new release will automatically be published on Sonatype.
   ```shell
   git push && git push --tags
   ```
7. Create a new release on GitHub for the newly-minted tag.
