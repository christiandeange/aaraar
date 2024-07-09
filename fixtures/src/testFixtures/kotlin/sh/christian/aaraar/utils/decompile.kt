package sh.christian.aaraar.utils

import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.INDENT_STRING
import org.jetbrains.java.decompiler.main.extern.IResultSaver
import sh.christian.aaraar.model.classeditor.ClassReference
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.nio.file.Files
import java.util.jar.Manifest

fun decompile(classReference: ClassReference): String {
  val writer = StringWriter()
  val decompiler = OneTimeDecompiler(classReference.toBytecode(), writer)
  decompiler.decompileContext()
  return writer.toString()
}

private class OneTimeDecompiler(
  bytecode: ByteArray,
  writer: Writer,
) : BaseDecompiler(
  ConstantBytecodeProvider(bytecode),
  PrintWriterSaver(PrintWriter(writer, true)),
  mapOf(INDENT_STRING to "    "),
  NoLogger,
) {
  init {
    addSource(Files.createTempFile("out", ".class").toFile())
  }
}

private class ConstantBytecodeProvider(private val bytecode: ByteArray) : IBytecodeProvider {
  override fun getBytecode(externalPath: String, internalPath: String?): ByteArray {
    return bytecode
  }
}

private class PrintWriterSaver(private val writer: PrintWriter) : IResultSaver {
  override fun saveClassFile(
    path: String,
    qualifiedName: String,
    entryName: String,
    content: String,
    mapping: IntArray?,
  ) {
    writer.write(content)
  }

  override fun saveClassEntry(
    path: String,
    archiveName: String,
    qualifiedName: String,
    entryName: String,
    content: String
  ) {
    writer.write(content)
  }

  override fun saveFolder(path: String) = Unit
  override fun copyFile(source: String, path: String, entryName: String) = Unit
  override fun createArchive(path: String, archiveName: String, manifest: Manifest) = Unit
  override fun saveDirEntry(path: String, archiveName: String, entryName: String) = Unit
  override fun copyEntry(source: String, path: String, archiveName: String, entry: String) = Unit
  override fun closeArchive(path: String, archiveName: String) = Unit
}

private object NoLogger : IFernflowerLogger() {
  override fun writeMessage(message: String?, severity: Severity?) = Unit
  override fun writeMessage(message: String?, severity: Severity?, t: Throwable?) = Unit
}
