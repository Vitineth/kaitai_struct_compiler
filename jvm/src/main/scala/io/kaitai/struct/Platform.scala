package io.kaitai.struct

import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TablesExtension

import java.util.Locale
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import java.util

object Platform {
  def toUpperLocaleInsensitive(s: String) = s.toUpperCase(Locale.ROOT)

  def markdownToHtml(s: String): String = {
    val extensions = util.Arrays.asList(TablesExtension.create())
    val parser = Parser
      .builder()
      .extensions(extensions)
      .build()
    val document = parser.parse(s)
    val renderer = HtmlRenderer
      .builder()
      .extensions(extensions)
      .build()
    renderer.render(document)
  }

}
