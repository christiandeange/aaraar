package sh.christian.mylibrary

class `Name-Maker` {
  fun configure(block: Name.() -> Unit): Name {
    val name = Name(DEFAULT_VALUE)
    name.apply(block)
    return name
  }

  companion object {
    val DEFAULT_VALUE = ""
  }
}
