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
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import sh.christian.aaraar.utils.toNode
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class Resources
private constructor(
  private val files: FileSet,
  private val packageName: String,
  private val minSdk: Int,
  private val androidAaptIgnore: String,
) : Mergeable<Resources> {
  override fun plus(others: List<Resources>): Resources {
    val consumer = ResourceMergerConsumer()

    ResourceMerger(minSdk).apply {
      // Add data sets in increasing order of priority.
      others.reversed().forEach { other ->
        addDataSet(other.files.toResourceSet(other.packageName, other.androidAaptIgnore, isFromDependency = true))
      }
      addDataSet(files.toResourceSet(packageName, androidAaptIgnore, isFromDependency = false))
      mergeData(consumer, /* doCleanUp */ false)
    }

    @OptIn(ExperimentalStdlibApi::class)
    val resourcePaths: Map<String, ByteArray> = buildMap {
      (consumer.files + consumer.generated).forEach { item ->
        val path = item.file.toPath()
        val outputPath = path.parent.fileName.resolve(path.fileName).toString()
        put(outputPath, Files.readAllBytes(item.file.toPath()))
      }

      consumer.values.forEach { (qualifiers, items) ->
        val mergedResourceValues = xml("resources") {
          items.forEach { item ->
            addNode(item.value.toNode())
          }
        }

        val path = if (qualifiers.isEmpty()) {
          "$FD_RES_VALUES/$FD_RES_VALUES.xml"
        } else {
          "$FD_RES_VALUES$RES_QUALIFIER_SEP$qualifiers/$FD_RES_VALUES.xml"
        }

        put(path, mergedResourceValues.toString(RESOURCE_VALUE_PRINT_OPTIONS).toByteArray())
      }
    }

    return Resources(FileSet.from(resourcePaths), packageName, minSdk, androidAaptIgnore)
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
        SINGLE_FILE -> files.remove(removedItem)
        GENERATED_FILES -> generated.remove(removedItem)
        XML_VALUES -> values[removedItem.qualifiers]!!.remove(removedItem)
      }

      replacedBy?.let(::addItem)
    }

    override fun end() = Unit

    override fun ignoreItemInMerge(item: ResourceMergerItem): Boolean = item.ignoredFromDiskMerge
  }

  companion object {
    private val RESOURCE_VALUE_PRINT_OPTIONS = PrintOptions(singleLineTextElements = true)

    fun from(
      path: Path,
      packageName: String,
      minSdk: Int,
      androidAaptIgnore: String,
    ): Resources {
      return FileSet.fromFileTree(path)
        ?.let { files ->
          Resources(
            files = files,
            packageName = packageName,
            minSdk = minSdk,
            androidAaptIgnore = androidAaptIgnore,
          )
        }
        ?: Resources(
          files = FileSet.EMPTY,
          packageName = packageName,
          minSdk = minSdk,
          androidAaptIgnore = androidAaptIgnore,
        )
    }

    private fun FileSet.toResourceSet(
      packageName: String,
      androidAaptIgnore: String,
      isFromDependency: Boolean,
    ): ResourceSet {
      return ResourceSet(packageName, RES_AUTO, null, false, androidAaptIgnore).apply {
        this.isFromDependency = isFromDependency
        setCheckDuplicates(false)

        val dir = Files.createTempDirectory("res-merger-$packageName")
        writeTo(dir)
        addSources(listOf(dir.toFile()))
        loadFromFiles(StdLogger(Level.WARNING))
      }
    }
  }
}
