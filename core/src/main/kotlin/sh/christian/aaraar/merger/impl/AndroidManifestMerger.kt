package sh.christian.aaraar.merger.impl

import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.MergingReport
import com.android.utils.StdLogger
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.model.AndroidManifest

/**
 * Standard implementation for merging multiple `AndroidManifest.xml` files.
 *
 * The basis of this implementation uses the same manifest merging logic that the Android Gradle Plugin uses.
 * The stock configuration is sensible for most libraries, but allows for customizing which features are enabled during
 * manifest merging. Some features like [Feature.USES_SDK_IN_MANIFEST_LENIENT_HANDLING] are only available on newer
 * versions of the Android Gradle Plugin, and will have no effect on older versions.
 */
class AndroidManifestMerger(
  vararg val features: Feature,
) : Merger<AndroidManifest> {

  private val placeholderValues = mutableMapOf<String, String>()

  override fun merge(
    first: AndroidManifest,
    others: List<AndroidManifest>,
  ): AndroidManifest {
    val mergeReport = ManifestMerger2.newMerger(
      first.asTempFile(),
      StdLogger(StdLogger.Level.WARNING),
      ManifestMerger2.MergeType.APPLICATION,
    )
      .apply {
        placeholderValues.forEach { (key, value) ->
          setPlaceHolderValue(key, value)
        }

        features.forEach { feature ->
          try {
            withFeatures(ManifestMerger2.Invoker.Feature.valueOf(feature.name))
          } catch (_: IllegalArgumentException) {
          }
        }
        others.forEach { other ->
          addLibraryManifest(other.asTempFile())
        }
      }
      .merge()

    check(mergeReport.result != MergingReport.Result.ERROR) {
      """
        Failed to merge manifest. ${mergeReport.reportString}

        ${mergeReport.loggingRecords.joinToString("\n")}
      """.trimIndent()
    }

    return AndroidManifest(mergeReport.getMergedDocument(MergingReport.MergedManifestKind.MERGED)!!)
  }

  fun setPlaceholder(
    key: String,
    value: String,
  ) {
    placeholderValues[key] = value
  }

  /**
   * Optional behavior of the merging tool can be turned on by setting these features during manifest merging.
   */
  enum class Feature {

    /**
     * Keep all intermediary merged files during the merging process.
     */
    KEEP_INTERMEDIARY_STAGES,

    /**
     * When logging file names, use [getName][java.io.File.getName] rather than [getPath][java.io.File.getPath].
     */
    PRINT_SIMPLE_FILENAMES,

    /**
     * Perform a sweep after all merging activities to remove all fully qualified class names and replace them with the
     * equivalent short version.
     */
    EXTRACT_FQCNS,

    /**
     * Perform a sweep after all merging activities to remove all `tools:` decorations.
     */
    REMOVE_TOOLS_DECLARATIONS,

    /**
     * Do no perform placeholders replacement.
     */
    NO_PLACEHOLDER_REPLACEMENT,

    /**
     * Encode unresolved placeholders to be AAPT friendly.
     */
    MAKE_AAPT_SAFE,

    /**
     * Clients will not request the blame history
     */
    SKIP_BLAME,

    /**
     * Clients will only request the merged XML documents, not XML pretty printed documents
     */
    SKIP_XML_STRING,

    /**
     * Add `android:testOnly="true"` attribute to prevent APK from being uploaded to Play store.
     */
    TEST_ONLY,

    /**
     * Do not perform implicit permission addition.
     */
    NO_IMPLICIT_PERMISSION_ADDITION,

    /**
     * Perform Studio advanced profiling manifest modifications.
     */
    ADVANCED_PROFILING,

    /**
     * Mark this application as a feature split.
     */
    ADD_DYNAMIC_FEATURE_ATTRIBUTES,

    /**
     * Set the `android:debuggable` flag to the application.
     */
    DEBUGGABLE,

    /**
     * When there are attribute value conflicts, automatically pick the higher priority value.
     *
     * This is for example used in the IDE when we need to merge a new manifest template into an existing one, and we
     * don't want to abort the merge.
     *
     * (This will log a warning.)
     */
    HANDLE_VALUE_CONFLICTS_AUTOMATICALLY,

    /**
     * Adds the AndroidX name of [MULTI_DEX_APPLICATION][com.android.AndroidXConstants.MULTI_DEX_APPLICATION] as
     * application name if none is specified. Used for legacy multidex.
     */
    ADD_ANDROIDX_MULTIDEX_APPLICATION_IF_NO_NAME,

    /**
     * Adds the pre-AndroidX name of [MULTI_DEX_APPLICATION][com.android.AndroidXConstants.MULTI_DEX_APPLICATION] as
     * application name if none is specified. Used for legacy multidex.
     */
    ADD_SUPPORT_MULTIDEX_APPLICATION_IF_NO_NAME,

    /*
     * Disables check for uniqueness of package names in dependencies manifests.
     */
    DISABLE_PACKAGE_NAME_UNIQUENESS_CHECK,

    /**
     * Enforce that dependencies manifests don't have duplicated package names.
     */
    ENFORCE_UNIQUE_PACKAGE_NAME,

    /**
     * Unsafely disables minSdkVersion check in libraries.
     */
    DISABLE_MINSDKLIBRARY_CHECK,

    /**
     * Warn if the `package` attribute is present in the main manifest, or throw an exception if it's present and not
     * equal to the component's namespace.
     *
     * This is used in the Android Gradle Plugin because users must migrate to the new namespace DSL.
     */
    CHECK_IF_PACKAGE_IN_MAIN_MANIFEST,

    /**
     * Removes target SDK for library manifest.
     */
    DISABLE_STRIP_LIBRARY_TARGET_SDK,

    /**
     * If set, merger will continue merging after any errors, allowing to surface errors in the "merged manifest" editor
     * view.
     */
    KEEP_GOING_AFTER_ERRORS,

    /**
     * Warn if the [ATTR_EXTRACT_NATIVE_LIBS][com.android.SdkConstants.ATTR_EXTRACT_NATIVE_LIBS] attribute is set to
     * true in a dependency manifest but not set to true in the merged manifest.
     */
    VALIDATE_EXTRACT_NATIVE_LIBS_FROM_DEPENDENCIES,

    /**
     * Suppresses the warning shown when `package` attribute is present in the main manifest, and is equal to the
     * component's namespace.
     */
    SUPPRESS_MANIFEST_PACKAGE_WARNING,

    CHECK_INSTANT_FLAG,

    /**
     * Warns instead of throwing an error when the main manifest contains `<uses-sdk>` for the purpose of minSdkVersion,
     * maxSdkVersion or targetSdkVersion. Starting with AGP 9.0.0 this tag will be allowed for the purpose of
     * `tools:overrideLibrary` only.
     */
    USES_SDK_IN_MANIFEST_LENIENT_HANDLING,

    /**
     * When androidTest manifest can have `replace` attributes. AGP merge it with generated manifest triggering false
     * warnings.
     */
    DISABLE_REPLACE_WARNING,
  }
}
