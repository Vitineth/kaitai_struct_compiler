package io.kaitai.struct

import io.kaitai.struct.datatype.DataType
import io.kaitai.struct.datatype.DataType.UserType
import io.kaitai.struct.exprlang.Ast
import io.kaitai.struct.format._
import io.kaitai.struct.languages.components.{LanguageCompiler, LanguageCompilerStatic}
import io.kaitai.struct.translators.{HTMLTranslator, PerlTranslator, PythonTranslator}

class HtmlClassCompiler(classSpecs: ClassSpecs, topClass: ClassSpec) extends DocClassCompiler(classSpecs, topClass) {

  import HtmlClassCompiler._

  translator = new HTMLTranslator(provider)

  override def outFileName(topClass: ClassSpec): String = s"${topClass.nameAsStr}.html"

  override def indent: String = ""

  override def fileHeader(topClass: ClassSpec): Unit = {
    out.puts(
      s"""
         |<!doctype html>
         |<html lang="en">
         |  <head>
         |    <!-- Required meta tags -->
         |    <meta charset="utf-8">
         |    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
         |
         |    <!-- Bootstrap CSS -->
         |    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css" integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS" crossorigin="anonymous">
         |
         |    <style>
         |    pre {
         |      background: white;
         |      padding: 7px;
         |      border: 1px solid #cbafbc;
         |      box-shadow: rgba(0, 0, 0, 0.06) 0px 2px 4px 0px inset;
         |    }
         |
         |    .pre-wrap{
         |      display: flex;
         |      flex-direction: row;
         |      flex-wrap: wrap;
         |      justify-content: space-between;
         |    }
         |    </style>
         |
         |    <title>${type2str(topClass.name.last)} format specification</title>
         |  </head>
         |  <body>
           <div class="container">
         |  <h1>${type2str(topClass.name.last)} format specification</h1>
         |
      """.stripMargin)

    // TODO: parse & output meta/title, meta/file-extensions, etc
  }

  override def fileFooter(topClass: ClassSpec): Unit = {
    out.puts(
      """
        |  </div>
        |    <!-- Optional JavaScript -->
        |    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
        |    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
        |    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js" integrity="sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut" crossorigin="anonymous"></script>
        |    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/js/bootstrap.min.js" integrity="sha384-B0UglyR+jN6CkvvICOB2joaf5I4l3gm9GU6Hc1og6Ls7i6U/mkkaduKaBhlAXv9k" crossorigin="anonymous"></script>
        |  </body>
        |</html>
      """.stripMargin)
  }

  override def classHeader(classSpec: ClassSpec): Unit = {
    out.puts(s"<a name='${classSpec2Anchor(classSpec)}'></a>")
    out.puts(s"<$headerByIndent>Type: ${type2str(classSpec.name.last)}</$headerByIndent>")
    out.puts

    classSpec.doc.summary.foreach(summary =>
      out.puts(s"<p>${Platform.markdownToHtml(summary)}</p>")
    )
    out.inc
  }

  override def classFooter(classSpec: ClassSpec): Unit = {
    out.dec
  }

  override def seqHeader(classSpec: ClassSpec): Unit = {
    out.puts("<table class=\"table\">")
    out.puts("<tr><th>Offset</th><th>Size</th><th>ID</th><th>Type</th><th>Note</th></tr>")
  }

  override def seqFooter(classSpec: ClassSpec): Unit = {
    out.puts("</table>")
  }

  override def compileSeqAttr(classSpec: ClassSpec, attr: AttrSpec, seqPos: Option[Int], sizeElement: Sized, sizeContainer: Sized): Unit = {
    out.puts("<tr>")
    out.puts(s"<td>${GraphvizClassCompiler.seqPosToStr(seqPos).getOrElse("???")}</td>")
    sizeElement match {
      case DynamicSized => out.puts(s"<td>dyn</td>")
      case FixedSized(n) => {
        if (n > 8 && n % 8 == 0) {
          out.puts(s"<td>${n / 8}&nbsp;byte${if (n > 8) "s" else ""}</td>")
        } else {
          out.puts(s"<td>${n}&nbsp;bit${if (n > 1) "s" else ""}</td>")
        }
      }
      case NotCalculatedSized => out.puts(s"<td>???</td>")
      case StartedCalculationSized => out.puts(s"<td>???</td>")
    }
    //    out.puts(s"<td>...</td>")
    out.puts(s"<td>${attr.id.humanReadable}</td>")
    out.puts(s"<td><code>${kaitaiType2NativeType(attr.dataType)}</code></td>")
    out.puts(s"<td>${Platform.markdownToHtml(attr.doc.summary.getOrElse("") + validatorToMarkdown(attr))}</td>")
    out.puts("</tr>")
  }

  def validatorToMarkdown(attr: AttrSpec): String = {
    /**
     * If attr.validator is defined, we want to produce an actual output
     */
    if (attr.valid.isEmpty) {
      return ""
    }

    val header = "\n\n------------------------\n\n**Validation Specification**\n\nThis field must "
    val spec = attr.valid.get
    spec match {
      case ValidationEq(value) => s"${header}satisfy the following constraint: \n\n```\n${attr.id.humanReadable} === ${expression(Some(value))}\n```"
      case ValidationExpr(checkExpr) => s"${header}satisfy the following constraint: \n\n```\n${expression(Some(checkExpr))}\n```"
      case ValidationRange(min, max) => s"${header}satisfy the following constraint: \n\n```\n${expression(Some(min))} <= ${attr.id.humanReadable} <= ${expression(Some(max))}\n```"
      case ValidationAnyOf(values) => s"${header}equal any of the following values: ${validationAnyOfToMarkdown(ValidationAnyOf(values))}"
      case ValidationMax(max) => s"${header}satisfy the following constraint: \n\n```\n${attr.id.humanReadable} <= ${expression(Some(max))}\n````"
      case ValidationMin(min) => s"${header}satisfy the following constraint: \n\n```\n${attr.id.humanReadable} >= ${expression(Some(min))}\n````"
      case _ => s"${header}satisfy an unknown constraint."
    }
  }

  def validationAnyOfToMarkdown(valid: ValidationAnyOf): String = {
    var output = "<div class=\"pre-wrap\">\n"
    for (elem <- valid.values) {
      output += s"\n\n```\n${expression(Some(elem))}\n```"
    }
    output += "\n</div>"
    output
  }

  override def compileParseInstance(classSpec: ClassSpec, inst: ParseInstanceSpec): Unit = {
    out.puts(s"<p><b>Parse instance</b>: ${inst.id.humanReadable}</p>")
    out.puts("<table class=\"table\">")
    out.puts("<tr>")
    out.puts(s"<td>${expression(inst.pos)}</td>")
    out.puts(s"<td>...</td>")
    out.puts(s"<td>${inst.id.humanReadable}</td>")
    out.puts(s"<td>${kaitaiType2NativeType(inst.dataType)}</td>")
    out.puts(s"<td>${Platform.markdownToHtml(inst.doc.summary.getOrElse(""))}</td>")
    out.puts("</tr>")
    out.puts("</table>")
  }

  override def compileValueInstance(vis: ValueInstanceSpec): Unit = {
    out.puts(s"value instance: ${vis}")
  }

  override def compileEnum(enumName: String, enumColl: EnumSpec): Unit = {
    out.puts(s"<a name='${enumSpec2Anchor(enumColl)}'></a>")
    out.puts(s"<$headerByIndent>Enum: $enumName</$headerByIndent>")
    out.puts

    out.puts("<table class=\"table\">")
    out.puts("<tr>")
    out.puts("<th>ID</th><th>Name</th><th>Note</th>")
    out.puts("</tr>")

    enumColl.sortedSeq.foreach { case (id, value) =>
      out.puts("<tr>")
      out.puts(s"<td>$id</td><td>${value.name}</td><td>${Platform.markdownToHtml(value.doc.summary.getOrElse(""))}</td></tr>")
      out.puts("</tr>")
    }

    out.puts("</table>")
  }

  def headerByIndent: String = s"h${out.indentLevel + 1}"

  def expression(exOpt: Option[Ast.expr]): String = {
    exOpt match {
      case Some(ex) => translator.translate(ex)
      case None => ""
    }
  }
}

object HtmlClassCompiler extends LanguageCompilerStatic {
  // FIXME: Unused, should be probably separated from LanguageCompilerStatic
  override def getCompiler(
                            tp: ClassTypeProvider,
                            config: RuntimeConfig
                          ): LanguageCompiler = ???

  def type2str(name: String): String = Utils.upperCamelCase(name)

  def classSpec2Anchor(spec: ClassSpec): String = "type-" + spec.name.mkString("-")

  def enumSpec2Anchor(spec: EnumSpec): String = "enum-" + spec.name.mkString("-")

  def kaitaiType2NativeType(attrType: DataType): String = attrType match {
    case ut: UserType =>
      "<a href=\"#" + classSpec2Anchor(ut.classSpec.get) + "\">" + type2str(ut.name.last) + "</a>"
    case _ => GraphvizClassCompiler.dataTypeName(attrType)
  }
}
