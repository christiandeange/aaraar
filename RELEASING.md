# Production Releases

1. Run the release script. Any unsaved local changes will be ignored.
   ```shell
   ./release.sh
   ```
2. Push the commits. A new release will automatically be published on Sonatype.
   ```shell
   git push && git push --tags
   ```
3. Create a new release on GitHub for the newly-minted tag.
