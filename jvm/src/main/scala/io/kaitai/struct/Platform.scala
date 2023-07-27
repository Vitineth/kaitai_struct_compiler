package io.kaitai.struct

import java.util.Locale
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object Platform {
  def toUpperLocaleInsensitive(s: String) = s.toUpperCase(Locale.ROOT)

  def markdownToHtml(s: String): String = {
    val parser = Parser.builder().build()
    val document = parser.parse(s)
    val renderer = HtmlRenderer.builder().build()
    renderer.render(document)
  }

}
