package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.Prefab

class PrefabMerger(
  private val fileSetMerger: Merger<FileSet>,
) : Merger<Prefab> {
  override fun merge(
    first: Prefab,
    others: List<Prefab>
  ): Prefab {
    val allPrefabs = (listOf(first) + others).filterNot { it == Prefab.NONE }

    return when (allPrefabs.size) {
      0 -> Prefab.NONE
      1 -> allPrefabs.single()
      else -> {
        val primaryPrefab = allPrefabs.first()
        val dependencyPrefabs = allPrefabs.drop(1)

        val mergedPackageMetadata = primaryPrefab.packageMetadata!!.copy(
          dependencies = dependencyPrefabs
            .mapNotNull { it.packageMetadata }
            .flatMap { listOf(it.name) + it.dependencies }
            .distinct(),
        )

        val mergedModules = fileSetMerger.merge(
          first = primaryPrefab.modules,
          others = dependencyPrefabs.map { it.modules },
        )

        Prefab(
          packageMetadata = mergedPackageMetadata,
          modules = mergedModules,
        )
      }
    }
  }
}
