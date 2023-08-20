package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.NavigationJson

/**
 * Standard implementation for merging multiple `navigation.json` files.
 *
 * Concatenates all file entries without any deduplication.
 */
class NavigationJsonMerger : Merger<NavigationJson> {
  override fun merge(first: NavigationJson, others: List<NavigationJson>): NavigationJson {
    return NavigationJson(first.navigationData + others.flatMap { it.navigationData })
  }
}
