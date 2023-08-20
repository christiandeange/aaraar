package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.NavigationJson

class NavigationJsonMerger : Merger<NavigationJson> {
  override fun merge(first: NavigationJson, others: List<NavigationJson>): NavigationJson {
    return NavigationJson(first.navigationData + others.flatMap { it.navigationData })
  }
}
