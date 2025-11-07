package sh.christian.aaraar.model

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import sh.christian.aaraar.utils.div
import java.nio.file.Files
import java.nio.file.Path

data class Prefab(
  val packageMetadata: PackageMetadata?,
  val modules: FileSet,
) {
  fun writeTo(path: Path) {
    if (packageMetadata != null || modules.isNotEmpty()) {
      Files.createDirectories(path)
    }

    if (packageMetadata != null) {
      val prefabJsonPath = path / "prefab.json"
      Files.writeString(prefabJsonPath, GSON.toJson(packageMetadata))
    }

    // Write the modules
    val modulesPath = path / "modules"
    modules.writeTo(modulesPath)
  }

  data class PackageMetadata(
    @SerializedName("schema_version")
    val schemaVersion: Int,
    val name: String,
    val version: String?,
    val dependencies: List<String>,
  ) {
    override fun toString(): String {
      return GSON.toJson(this)
    }
  }

  companion object {
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    val NONE = Prefab(
      packageMetadata = null,
      modules = FileSet.EMPTY,
    )

    fun from(path: Path): Prefab {
      if (!Files.isDirectory(path)) return NONE

      val prefabJson = path / "prefab.json"
      val packageMetadata = GSON.fromJson(Files.newBufferedReader(prefabJson), PackageMetadata::class.java)
      val modules = FileSet.fromFileTree(path / "modules") ?: FileSet.EMPTY

      return Prefab(
        packageMetadata = packageMetadata,
        modules = modules,
      )
    }
  }
}
