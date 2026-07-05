package javassist

import sh.christian.aaraar.model.classeditor.MutableClassReference

internal fun MutableClassReference.setWasChanged() {
  (_class as? CtClassType)?.wasChanged = true
}

internal fun MutableClassReference.unsetWasChanged() {
  (_class as? CtClassType)?.wasChanged = false
}
