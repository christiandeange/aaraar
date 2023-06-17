package sh.christian.aaraar.model

import com.android.manifmerger.NavigationXmlDocumentData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path

class NavigationJson
internal constructor(
  val navigationData: List<NavigationXmlDocumentData>,
) : Mergeable<NavigationJson> {
  override operator fun plus(others: List<NavigationJson>): NavigationJson {
    return NavigationJson(navigationData + others.flatMap { it.navigationData })
  }

  fun writeTo(path: Path) {
    if (navigationData.isEmpty()) {
      Files.deleteIfExists(path)
    } else {
      Files.writeString(
        path,
        GSON.toJson(navigationData),
      )
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
  }
}
