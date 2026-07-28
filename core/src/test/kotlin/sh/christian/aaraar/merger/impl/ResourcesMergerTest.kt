package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.Resources
import sh.christian.aaraar.utils.forEntry
import kotlin.test.Test

class ResourcesMergerTest {

  private val merger = ResourcesMerger()

  @Test
  fun `merges same-named resources of different types cleanly`() {
    val stringResources = resources(
      "values/strings.xml" to
        """
        <resources>
            <string name="margin">Hello</string>
        </resources>
        """,
    )

    val dimenResources = resources(
      "values/dimens.xml" to
        """
        <resources>
            <dimen name="margin">16dp</dimen>
        </resources>
        """,
    )

    val merged = merger.merge(stringResources, dimenResources)

    merged.files.forEntry("values/values.xml") shouldHaveFileContents """
      <resources>
          <dimen name="margin">16dp</dimen>
          <string name="margin">Hello</string>
      </resources>
    """
  }

  @Test
  fun `merges same-named resources of same types with library definition taking priority`() {
    val libraryResources = resources(
      "values/strings.xml" to
        """
        <resources>
            <string name="greeting">Hello from library!</string>
        </resources>
        """,
    )

    val dependencyResources = resources(
      "values/strings.xml" to
        """
        <resources>
            <string name="greeting">Hello from dependency!</string>
            <string name="dependency">dependency</string>
        </resources>
        """,
    )

    val merged = merger.merge(libraryResources, dependencyResources)

    merged.files.forEntry("values/values.xml") shouldHaveFileContents """
      <resources>
          <string name="greeting">Hello from library!</string>
          <string name="dependency">dependency</string>
      </resources>
    """
  }

  @Test
  fun `merges same-named resources of same types with qualifiers being kept`() {
    val libraryResources = resources(
      "values/strings.xml" to
        """
        <resources>
            <string name="greeting">Hello from USA!</string>
        </resources>
        """,
    )

    val dependencyResources = resources(
      "values/strings.xml" to
        """
        <resources>
            <string name="greeting">Hello from USA!</string>
        </resources>
        """,
      "values-gb/strings.xml" to
        """
        <resources>
            <string name="greeting">Hello from the UK!</string>
        </resources>
        """,
    )

    val merged = merger.merge(libraryResources, dependencyResources)

    merged.files.forEntry("values/values.xml") shouldHaveFileContents """
      <resources>
          <string name="greeting">Hello from USA!</string>
      </resources>
    """

    merged.files.forEntry("values-gb/values.xml") shouldHaveFileContents """
      <resources>
          <string name="greeting">Hello from the UK!</string>
      </resources>
    """
  }

  private fun resources(vararg files: Pair<String, String>): Resources {
    return Resources(
      files = FileSet(files.associate { (path, contents) -> path to contents.trimIndent().toByteArray() }),
      packageName = "sh.christian.library",
      minSdk = 21,
      androidAaptIgnore = "",
    )
  }
}
