`aaraar` is a Gradle plugin that assists in embedding other Gradle dependencies into your published artifact.
The plugin can be applied to any module that is published as an `aar` or a `jar` file, and includes some handy features
such as:

- Embedding dependencies directly into your `jar` or `aar` file
- Shading classes to rename or delete them
- Stripping `META-INF` files

`aaraar` is simple to use with the most common publishing plugins, including the
[Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html) and the
[Gradle Maven Publish Plugin](https://github.com/vanniktech/gradle-maven-publish-plugin), but advanced configuration is
still available for those with a more custom publishing pipeline.

Visit [Installation](installation.md) to get started.
