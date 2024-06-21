package sh.christian.aaraar.merger

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlobTest {

  @Test
  fun `matches literal name`() {
    glob("""hello.txt""").run {
      shouldMatch("hello.txt")
    }
  }

  @Test
  fun `asterisk matches within folder`() {
    glob("""libs/*.so""").run {
      shouldMatch("libs/libc.so")
      shouldNotMatch("libs/x86/libc.so")
    }
  }

  @Test
  fun `double asterisk matches across subfolders`() {
    glob("""libs/**.so""").run {
      shouldMatch("libs/libc.so")
      shouldMatch("libs/x86/libc.so")
      shouldMatch("libs/x86/64/libc.so")
    }
  }

  @Test
  fun `question mark matches a single character`() {
    glob("""release?.md""").run {
      shouldMatch("release1.md")
      shouldMatch("release2.md")
      shouldMatch("released.md")
      shouldNotMatch("release10.md")
      shouldNotMatch("release/.md")
    }

    glob("""release..md""").run {
      shouldNotMatch("release1.md") // unlike regexes, a dot is not a meta-character in globs
    }
  }

  @Test
  fun `backslash escapes a meta character`() {
    glob("""release\?.md""").run {
      shouldMatch("release?.md")
      shouldNotMatch("release1.md")
    }
  }

  @Test
  fun `square brackets match any of once`() {
    glob("""release[12].md""").run {
      shouldMatch("release1.md")
      shouldMatch("release2.md")
      shouldNotMatch("release3.md")
    }

    glob("""release[0-9].md""").run {
      shouldMatch("release1.md")
      shouldMatch("release9.md")
      shouldNotMatch("released.md")
      shouldNotMatch("release-.md")
      shouldNotMatch("release10.md")
    }
  }

  @Test
  fun `negated square brackets match none of once`() {
    glob("""release[!12].md""").run {
      shouldNotMatch("release1.md")
      shouldNotMatch("release2.md")
      shouldMatch("release3.md")
    }

    glob("""release[!0-9].md""").run {
      shouldNotMatch("release1.md")
      shouldNotMatch("release9.md")
      shouldMatch("released.md")
      shouldMatch("release-.md")
      shouldNotMatch("release10.md")
    }
  }

  @Test
  fun `curly braces match any of a group`() {
    glob("""{release,debug}.md""").run {
      shouldMatch("release.md")
      shouldMatch("debug.md")
      shouldNotMatch("release1.md")
    }

    glob("""{release,debug}{1,2}.md""").run {
      shouldMatch("release1.md")
      shouldMatch("release2.md")
      shouldMatch("debug1.md")
      shouldMatch("debug2.md")
      shouldNotMatch("release.md")
    }
  }

  @Test
  fun `leading dot in file name is normal`() {
    glob("""*.login""").run {
      shouldMatch(".login")
    }
  }

  private fun glob(glob: String): Glob {
    return Glob.fromString(glob.replace('/', File.separatorChar))
  }

  private fun Glob.shouldMatch(target: String) {
    assertTrue("Glob '$this' should match '$target'") { matches(target) }
  }

  private fun Glob.shouldNotMatch(target: String) {
    assertFalse("Glob '$this' should not match '$target'") { matches(target) }
  }
}
