package sh.christian.aaraar.model

import com.android.ide.common.rendering.api.ResourceNamespace.RES_AUTO
import com.android.ide.common.resources.DataFile.FileType.*
import com.android.ide.common.resources.MergeConsumer
import com.android.ide.common.resources.ResourceMerger
import com.android.ide.common.resources.ResourceMergerItem
import com.android.ide.common.resources.ResourceSet
import com.android.resources.ResourceConstants.FD_RES_VALUES
import com.android.resources.ResourceConstants.RES_QUALIFIER_SEP
import com.android.utils.StdLogger
import com.android.utils.StdLogger.Level
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.writeTo
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory


class Resources
private constructor(
  private val files: FileSet,
  private val packageName: String,
  private val minSdk: Int,
) : Mergeable<Resources> {
  fun isEmpty(): Boolean = files.isEmpty()

  override fun plus(others: List<Resources>): Resources {
    fun FileSet.toResourceSet(isFromDependency: Boolean): ResourceSet {
      return ResourceSet(packageName, RES_AUTO, null, false, "").apply {
        this.isFromDependency = isFromDependency
        setCheckDuplicates(false)

        val dir = Files.createTempDirectory("res-merger-$packageName")
        writeTo(dir)
        addSources(listOf(dir.toFile()))
        loadFromFiles(StdLogger(Level.WARNING))
      }
    }

    val consumer = ResourceMergerConsumer()

    ResourceMerger(minSdk).apply {
      addDataSet(files.toResourceSet(isFromDependency = false))
      others.forEach { other ->
        addDataSet(other.files.toResourceSet(isFromDependency = true))
      }
      mergeData(consumer, false)
    }

    @OptIn(ExperimentalStdlibApi::class)
    val resourcePaths: Map<Path, ByteArray> = buildMap {
      (consumer.files + consumer.generated).forEach { item ->
        val path = item.file.toPath()
        val outputPath = path.parent.fileName.resolve(path.fileName)
        put(outputPath, Files.readAllBytes(item.file.toPath()))
      }

      consumer.values.forEach { (qualifiers, items) ->
        val document = consumer.factory.newDocumentBuilder().newDocument()
        val resources = document.createElement("resources")
        document.appendChild(resources)
        items.forEach { item ->
          resources.appendChild(document.adoptNode(item.value))
        }

        val path = if (qualifiers.isEmpty()) {
          files.fileSystem / FD_RES_VALUES / "$FD_RES_VALUES.xml"
        } else {
          files.fileSystem / "$FD_RES_VALUES$RES_QUALIFIER_SEP$qualifiers" / "$FD_RES_VALUES.xml"
        }

        val outputStream = ByteArrayOutputStream()
        document.writeTo(OutputStreamWriter(outputStream))
        val xmlBytes: ByteArray = outputStream.toByteArray()

        put(path, xmlBytes)
      }
    }

    return Resources(FileSet.from(resourcePaths), packageName, minSdk)
  }

  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  private class ResourceMergerConsumer : MergeConsumer<ResourceMergerItem> {
    val files = mutableListOf<ResourceMergerItem>()
    val generated = mutableListOf<ResourceMergerItem>()
    val values = mutableMapOf<String, MutableList<ResourceMergerItem>>()

    lateinit var factory: DocumentBuilderFactory

    override fun start(factory: DocumentBuilderFactory) {
      this.factory = factory
    }

    override fun addItem(item: ResourceMergerItem) {
      when (item.sourceType!!) {
        SINGLE_FILE -> files.add(item)
        GENERATED_FILES -> generated.add(item)
        XML_VALUES -> values.getOrPut(item.qualifiers) { mutableListOf() }.add(item)
      }
    }

    override fun removeItem(removedItem: ResourceMergerItem, replacedBy: ResourceMergerItem?) {
      when (removedItem.sourceType!!) {
        SINGLE_FILE -> files.add(removedItem)
        GENERATED_FILES -> generated.add(removedItem)
        XML_VALUES -> values[removedItem.qualifiers]!!.remove(removedItem)
      }

      replacedBy?.let(::addItem)
    }

    override fun end() = Unit

    override fun ignoreItemInMerge(item: ResourceMergerItem): Boolean = item.ignoredFromDiskMerge
  }

  companion object {
    fun from(
      path: Path,
      packageName: String,
      minSdk: Int,
    ): Resources {
      return FileSet.fromFileTree(path)
        ?.let { files ->
          Resources(
            files = files,
            packageName = packageName,
            minSdk = minSdk,
          )
        }
        ?: Resources(
          files = FileSet.EMPTY,
          packageName = packageName,
          minSdk = minSdk,
        )
    }
  }
}
