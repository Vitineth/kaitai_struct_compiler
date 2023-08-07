package io.kaitai.struct

import com.karasiq.markedjs.{Marked, MarkedOptions}

import scala.scalajs.js.undefined

object Platform {
    def toUpperLocaleInsensitive(s: String) = s.toUpperCase()

  def markdownToHtml(s: String): String = {
    Marked(s, MarkedOptions(
      gfm = true,
      tables = true,
    ))
  }
}
