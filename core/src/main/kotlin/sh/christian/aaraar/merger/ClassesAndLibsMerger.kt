package sh.christian.aaraar.merger

import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.Libs

interface ClassesAndLibsMerger : Merger<Classes> {
  fun merge(first: Classes, others: Libs): Classes
}
