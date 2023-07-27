package io.kaitai.struct

import com.karasiq.markedjs.Marked

object Platform {
    def toUpperLocaleInsensitive(s: String) = s.toUpperCase()

  def markdownToHtml(s: String): String = {
    Marked(s)
  }
}
