package io.kaitai.struct.translators

import io.kaitai.struct.ImportList
import io.kaitai.struct.datatype.DataType
import io.kaitai.struct.exprlang.Ast

class HTMLTranslator(provider: TypeProvider) extends JavaTranslator(provider, new ImportList()) {
  override def doByteArrayLiteral(arr: Seq[Byte]): String =
    s"[ ${arr.mkString(", ")} ]"
}
