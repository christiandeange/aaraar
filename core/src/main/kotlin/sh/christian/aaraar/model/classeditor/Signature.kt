package sh.christian.aaraar.model.classeditor

sealed interface Signature {
  val memberName: String
  val descriptor: String
}

data class ConstructorSignature(
  override val descriptor: String
) : Signature {
  override val memberName: String = "<init>"
}

data class MethodSignature(
  override val memberName: String,
  override val descriptor: String,
) : Signature

data class FieldSignature(
  override val memberName: String,
  override val descriptor: String,
) : Signature
