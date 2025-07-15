package sh.christian.aaraar.shading.impl.transform.config

internal object PatternUtils {
  // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.2
  private val disallowedJvmChars = setOf('.', ';', '[', '/')

  private val dstar: Regex = Regex("""\*\*""")
  private val star: Regex = Regex("""\*""")
  private val estar: Regex = Regex("""\+\??\)\Z""")
  private val nested: Regex = Regex("""\$""")

  fun newPattern(regex: String): Regex {
    require(regex != "**") {
      "'**' is not a valid pattern"
    }
    require(isPossibleQualifiedName(regex, "/*")) {
      "Not a valid package pattern: $regex"
    }
    require("***" !in regex) {
      "The sequence '***' is invalid in a package pattern"
    }

    return Regex(
      escapeComponents(regex)
        // One wildcard test requires the argument to be allowably empty.
        .let { replaceAllLiteral(it, dstar, """(.+?)""") }
        .let { replaceAllLiteral(it, star, """([^/]+)""") }
        // Although we replaced with + above, we mean *
        .let { replaceAllLiteral(it, estar, """*\??)""") }
        // Convert nested class symbols to regular expressions
        .let { replaceAllLiteral(it, nested, """\$""") }
        .let { """\A$it\Z""" }
    )
  }

  fun newReplace(result: String): List<ReplacePart> {
    var isEscape = false
    var i = 0
    var mark = 0
    val len = result.length

    return buildList {
      while (i < len + 1) {
        val ch = if (i > result.lastIndex) '@' else result[i]
        if (!isEscape) {
          if (ch == '@') {
            val text = result.substring(mark, i).replace('.', '/')
            add(ReplacePart.Literal(text))

            mark = i + 1
            isEscape = true
          }
        } else {
          if (!ch.isDigit()) {
            require(i != mark) { "@ not followed by a digit" }
            val group = result.substring(mark, i).toInt()
            add(ReplacePart.Group(group))

            mark = i--
            isEscape = false
          }
        }
        i++
      }
    }
  }

  fun replace(pattern: AbstractPattern, replace: List<ReplacePart>, value: String): String? {
    val matchResult = pattern.getMatchResult(value) ?: return null

    return replace.joinToString("") { part ->
      when (part) {
        is ReplacePart.Literal -> part.value
        is ReplacePart.Group -> matchResult.groupValues[part.index]
      }
    }
  }

  fun isPossibleQualifiedName(value: String, extraAllowedCharacters: String): Boolean {
    return value.all { it !in disallowedJvmChars || it in extraAllowedCharacters }
  }

  private fun replaceAllLiteral(value: String, regex: Regex, replace: String): String {
    return regex.replace(value, Regex.escapeReplacement(replace))
  }

  private fun escapeComponents(s: String): String {
    return s.split(".").joinToString(".") { if ('*' in it) it else Regex.escape(it) }
  }
}
