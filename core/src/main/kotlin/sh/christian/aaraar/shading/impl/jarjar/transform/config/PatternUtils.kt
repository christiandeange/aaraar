package sh.christian.aaraar.shading.impl.jarjar.transform.config

import java.util.regex.Matcher
import java.util.regex.Pattern

internal object PatternUtils {
  const val PACKAGE_INFO: String = "package-info"

  private val dstar: Pattern = Pattern.compile("\\*\\*")
  private val star: Pattern = Pattern.compile("\\*")
  private val estar: Pattern = Pattern.compile("\\+\\??\\)\\Z")
  private val nested: Pattern = Pattern.compile("\\$")

  fun newPattern(pattern: String): Pattern {
    require(pattern != "**") {
      "'**' is not a valid pattern"
    }
    require(isPossibleQualifiedName(pattern, "/*")) {
      "Not a valid package pattern: $pattern"
    }
    require("***" !in pattern) {
      "The sequence '***' is invalid in a package pattern"
    }

    return Pattern.compile(
      escapeComponents(pattern)
        // One wildcard test requires the argument to be allowably empty.
        .let { replaceAllLiteral(it, dstar, "(.+?)") }
        .let { replaceAllLiteral(it, star, "([^/]+)") }
        // Although we replaced with + above, we mean *
        .let { replaceAllLiteral(it, estar, "*\\??)") }
        // Convert nested class symbols to regular expressions
        .let { replaceAllLiteral(it, nested, "\\$") }
        .let { "\\A$it\\Z" }
    )
  }

  fun newReplace(pattern: Pattern, result: String): List<ReplacePart> {
    val parts = mutableListOf<ReplacePart>()
    var isEscape = false
    var i = 0
    var mark = 0
    val len = result.length

    while (i < len + 1) {
      val ch = if (i > result.lastIndex) '@' else result[i]
      if (!isEscape) {
        if (ch == '@') {
          parts.add(ReplacePart.Literal(result.substring(mark, i).replace('.', '/')))
          mark = i + 1
          isEscape = true
        }
      } else {
        if (!ch.isDigit()) {
          require(i != mark) { "@ not followed by a digit" }
          val n = result.substring(mark, i).toInt()
          parts.add(ReplacePart.Group(n))
          mark = i--
          isEscape = false
        }
      }
      i++
    }

    val count = pattern.matcher("foo").groupCount()
    val max = parts.filterIsInstance<ReplacePart.Group>().maxOfOrNull { it.index } ?: 0
    require(count >= max) {
      "Result includes impossible placeholder \"@$max\": $result"
    }

    return parts
  }

  fun replace(pattern: AbstractPattern, replace: List<ReplacePart>, value: String): String? {
    val matcher = pattern.getMatcher(value) ?: return null

    return replace.joinToString("") { part ->
      when (part) {
        is ReplacePart.Literal -> part.value
        is ReplacePart.Group -> matcher.group(part.index)
      }
    }
  }

  fun isPossibleQualifiedName(value: String, extraAllowedCharacters: String): Boolean {
    // package-info violates the spec for Java Identifiers.
    // Nevertheless, expressions that end with this string are still legal.
    // See 7.4.1.1 of the Java language spec for discussion.
    return value
      .removeSuffix(PACKAGE_INFO)
      .all { it.isJavaIdentifierPart() || it in extraAllowedCharacters }
  }

  private fun replaceAllLiteral(value: String, pattern: Pattern, replace: String): String {
    return pattern.matcher(value).replaceAll(Matcher.quoteReplacement(replace))
  }

  private fun escapeComponents(s: String): String {
    return s.split(".").joinToString(".") { if ('*' in it) it else Pattern.quote(it) }
  }
}
