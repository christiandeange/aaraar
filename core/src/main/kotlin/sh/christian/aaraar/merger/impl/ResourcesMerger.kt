package sh.christian.aaraar.merger.impl

import com.android.ide.common.rendering.api.ResourceNamespace
import com.android.ide.common.resources.DataFile
import com.android.ide.common.resources.MergeConsumer
import com.android.ide.common.resources.ResourceMergerItem
import com.android.ide.common.resources.ResourceSet
import com.android.resources.ResourceConstants.FD_RES_VALUES
import com.android.resources.ResourceConstants.RES_QUALIFIER_SEP
import com.android.utils.StdLogger
import org.redundent.kotlin.xml.Namespace
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.parse
import org.redundent.kotlin.xml.xml
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.Resources
import sh.christian.aaraar.utils.toNode
import java.io.File
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory
import com.android.ide.common.resources.ResourceMerger as AndroidResourceMerger

/**
 * Standard implementation for merging all resource values and resource files.
 *
 * The basis of this implementation uses the same resource merging logic that the Android Gradle Plugin uses.
 */
class ResourcesMerger : Merger<Resources> {
  override fun merge(first: Resources, others: List<Resources>): Resources {
    val consumer = ResourceMergerConsumer()

    AndroidResourceMerger(first.minSdk).apply {
      // Add data sets in increasing order of priority.
      others.reversed().forEach { other ->
        addDataSet(other.files.toResourceSet(other.packageName, other.androidAaptIgnore, isFromDependency = true))
      }
      addDataSet(first.files.toResourceSet(first.packageName, first.androidAaptIgnore, isFromDependency = false))
      mergeData(consumer, false)
    }

    val resourcePaths: Map<String, ByteArray> = buildMap {
      (consumer.files + consumer.generated).forEach { item ->
        val path = item.file.toPath()
        val outputPath = path.parent.fileName.resolve(path.fileName).toString()
        put(outputPath, Files.readAllBytes(item.file.toPath()))
      }

      consumer.values.forEach { (qualifiers, items) ->
        val mergedResourceValues = xml("resources") {
          consumer.namespaces
            .filter { it.key.second == qualifiers }
            .flatMap { it.value }
            .toSet()
            .forEach { namespace -> namespace(namespace) }

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

    return Resources(FileSet.from(resourcePaths), first.packageName, first.minSdk, first.androidAaptIgnore)
  }

  private class ResourceMergerConsumer : MergeConsumer<ResourceMergerItem> {
    val files = mutableListOf<ResourceMergerItem>()
    val generated = mutableListOf<ResourceMergerItem>()
    val values = mutableMapOf<String, MutableList<ResourceMergerItem>>()
    val namespaces = mutableMapOf<Pair<File, String>, Set<Namespace>>()

    lateinit var factory: DocumentBuilderFactory

    override fun start(factory: DocumentBuilderFactory) {
      this.factory = factory
    }

    override fun addItem(item: ResourceMergerItem) {
      when (item.sourceType!!) {
        DataFile.FileType.SINGLE_FILE -> files.add(item)
        DataFile.FileType.GENERATED_FILES -> generated.add(item)
        DataFile.FileType.XML_VALUES -> {
          namespaces.computeIfAbsent(Pair(item.file, item.qualifiers)) { _ ->
            if (item.file != null) {
              parse(item.file).namespaces.toSet()
            } else {
              emptySet()
            }
          }

          values.getOrPut(item.qualifiers) { mutableListOf() }.add(item)
        }
      }
    }

    override fun removeItem(removedItem: ResourceMergerItem, replacedBy: ResourceMergerItem?) {
      when (removedItem.sourceType!!) {
        DataFile.FileType.SINGLE_FILE -> files.remove(removedItem)
        DataFile.FileType.GENERATED_FILES -> generated.remove(removedItem)
        DataFile.FileType.XML_VALUES -> values[removedItem.qualifiers]!!.remove(removedItem)
      }

      replacedBy?.let(::addItem)
    }

    override fun end() = Unit

    override fun ignoreItemInMerge(item: ResourceMergerItem): Boolean = item.ignoredFromDiskMerge
  }

  companion object {
    private val RESOURCE_VALUE_PRINT_OPTIONS = PrintOptions(singleLineTextElements = true)

    private fun FileSet.toResourceSet(
      packageName: String,
      androidAaptIgnore: String,
      isFromDependency: Boolean,
    ): ResourceSet {
      return ResourceSet(packageName, ResourceNamespace.RES_AUTO, null, false, androidAaptIgnore).apply {
        this.isFromDependency = isFromDependency
        setCheckDuplicates(false)

        val dir = Files.createTempDirectory("res-merger-$packageName")
        writeTo(dir)
        addSource(dir.toFile())
        loadFromFiles(StdLogger(StdLogger.Level.WARNING))
      }
    }
  }
}
