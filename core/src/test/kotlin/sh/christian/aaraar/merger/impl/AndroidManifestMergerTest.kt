package sh.christian.aaraar.merger.impl

import sh.christian.aaraar.merger.impl.AndroidManifestMerger.Feature
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.utils.shouldBe
import kotlin.test.Test

class AndroidManifestMergerTest {

  private val defaultMerger = AndroidManifestMerger()

  private val featuredMerger = AndroidManifestMerger(
    Feature.NO_PLACEHOLDER_REPLACEMENT,
    Feature.REMOVE_TOOLS_DECLARATIONS,
    Feature.SKIP_BLAME,
    Feature.SKIP_XML_STRING,
    Feature.USES_SDK_IN_MANIFEST_LENIENT_HANDLING,
  )

  private val debuggableMerger = AndroidManifestMerger(
    Feature.DEBUGGABLE,
  )

  private val testOnlyMerger = AndroidManifestMerger(
    Feature.TEST_ONLY,
  )

  @Test
  fun `keeps package name from main manifest file`() {
    val mainLibManifest = AndroidManifest("""<manifest package="com.library.main" />""")
    val helperModuleManifest = AndroidManifest("""<manifest package="com.library.helper.core" />""")

    defaultMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """
  }

  @Test
  fun `adds new nodes from dependencies`() {
    val mainLibManifest = AndroidManifest("""<manifest package="com.library.main" />""")

    val helperModuleManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
      </manifest>
      """,
    )

    defaultMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
          <application/>
      </manifest>
      """
  }

  @Test
  fun `applies manifest placeholders rules`() {
    val manifest = AndroidManifest(
      $$"""
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application android:label="${applicationLabel}"/>
      </manifest>
      """,
    )

    featuredMerger.setPlaceholder("applicationLabel", "Library")
    defaultMerger.setPlaceholder("applicationLabel", "Library")

    featuredMerger.merge(manifest, emptyList()) shouldBe $$"""
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application android:label="${applicationLabel}"/>
      </manifest>
      """

    defaultMerger.merge(manifest, emptyList()) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application android:label="Library"/>
      </manifest>
      """
  }

  @Test
  fun `applies tool rules to manifests from dependencies`() {
    val mainLibManifest = AndroidManifest(
      """
      <manifest
              xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:tools="http://schemas.android.com/tools"
              package="com.library.main">

          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" tools:node="remove" />
      </manifest>
      """,
    )

    val helperModuleManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
      </manifest>
      """,
    )

    featuredMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """

    defaultMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools" package="com.library.main">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" tools:node="remove"/>
          <application/>
      </manifest>
      """
  }

  @Test
  fun `keeps tool rules to manifests from dependencies when configured without it`() {
    val mainLibManifest = AndroidManifest(
      """
      <manifest
              xmlns:android="http://schemas.android.com/apk/res/android"
              xmlns:tools="http://schemas.android.com/tools"
              package="com.library.main">

          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" tools:node="remove" />
      </manifest>
      """,
    )

    val helperModuleManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
      </manifest>
      """,
    )

    defaultMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools" package="com.library.main">
          <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" tools:node="remove"/>
          <application/>
      </manifest>
      """

    featuredMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """
  }

  @Test
  fun `test only merger`() {
    val mainLibManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """,
    )
    val helperModuleManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <application/>
      </manifest>
      """,
    )

    testOnlyMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application android:testOnly="true"/>
      </manifest>
      """
  }

  @Test
  fun `debuggable merger`() {
    val mainLibManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application/>
      </manifest>
      """,
    )
    val helperModuleManifest = AndroidManifest(
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.helper.core">
          <application/>
      </manifest>
      """,
    )

    debuggableMerger.merge(mainLibManifest, helperModuleManifest) shouldBe """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.library.main">
          <application android:debuggable="true"/>
      </manifest>
      """
  }
}
