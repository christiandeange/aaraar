package sh.christian.mylibrary

class Name(var name: String) {
  fun printName() {
    println("Name: $name")
  }

  fun updateName(newName: String) {
    println("Name updated: $newName")
  }
}
