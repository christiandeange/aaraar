package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.ApiJar
import sh.christian.aaraar.model.GenericJarArchive

/**
 * Standard jar-wise implementation for merging multiple `api.jar` files.
 */
class ApiJarMerger(
  private val jarMerger: Merger<GenericJarArchive>,
) : Merger<ApiJar> {
  override fun merge(first: ApiJar, others: List<ApiJar>): ApiJar {
    return ApiJar(jarMerger.merge(first.archive, others.map { it.archive }))
  }
}
