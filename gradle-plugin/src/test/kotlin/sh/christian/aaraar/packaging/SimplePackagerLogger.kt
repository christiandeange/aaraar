package sh.christian.aaraar.packaging

class SimplePackagerLogger : PackagerLogger {
  override fun info(message: String) {
    println("INFO: $message")
  }

  override fun warning(message: String) {
    println("WARNING: $message")
  }

  override fun warning(message: String, exception: Exception) {
    println("WARNING: $message")
    exception.printStackTrace()
  }

  override fun error(message: String) {
    println("ERROR: $message")
  }

  override fun error(message: String, exception: Exception) {
    println("ERROR: $message")
    exception.printStackTrace()
  }
}
