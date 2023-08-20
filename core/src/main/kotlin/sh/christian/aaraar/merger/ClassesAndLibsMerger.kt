package sh.christian.aaraar.merger

import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.Libs

/**
 * Used for implementations that merge `libs/` jars into a main `classes.jar` file.
 */
interface ClassesAndLibsMerger : Merger<Classes> {
  fun merge(first: Classes, others: Libs): Classes
}
