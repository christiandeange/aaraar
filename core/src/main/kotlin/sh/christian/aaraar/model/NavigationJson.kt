package sh.christian.aaraar.model

import com.android.manifmerger.NavigationXmlDocumentData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents the contents of the `navigation.json` file.
 */
class NavigationJson
internal constructor(
  internal val navigationData: List<NavigationXmlDocumentData>,
) {
  constructor(jsonSource: String) : this(parseJson(jsonSource))

  override fun toString(): String {
    return GSON.toJson(navigationData)
  }

  override fun equals(other: Any?): Boolean {
    if (other !is NavigationJson) return false
    return navigationData == other.navigationData
  }

  override fun hashCode(): Int {
    return navigationData.hashCode()
  }

  fun writeTo(path: Path) {
    if (navigationData.isEmpty()) {
      Files.deleteIfExists(path)
    } else {
      Files.writeString(path, toString())
    }
  }

  companion object {
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    fun from(path: Path): NavigationJson {
      if (!Files.isRegularFile(path)) return NavigationJson(emptyList())

      val typeToken = object : TypeToken<List<NavigationXmlDocumentData>>() {}.type
      val navigationData = GSON.fromJson(Files.newBufferedReader(path), typeToken) as List<NavigationXmlDocumentData>

      return NavigationJson(navigationData)
    }

    private fun parseJson(json: String): List<NavigationXmlDocumentData> {
      val typeToken = object : TypeToken<List<NavigationXmlDocumentData>>() {}.type
      return GSON.fromJson(json, typeToken) as List<NavigationXmlDocumentData>
    }
  }
}
