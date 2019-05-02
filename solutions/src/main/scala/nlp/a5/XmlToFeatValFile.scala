package nlp.a5

import dhg.util.FileUtil._
import dhg.util.CollectionUtil._
import dhg.util.CollectionUtil.KeepDelimiter._
import dhg.util.StringUtil._
import dhg.util.Pattern._
import dhg.util.Time._
import dhg.util.CommandLineUtil._
import chalk.lang.eng.Twokenize

object XmlToFeatValFile {

  val BannedSymbols = Set("=", ",")

  val ItemLineRe = """<item(.*)>""".r
  val ContentLineRe = """<content>(.*)</content>""".r
  val QuotedRe = """"(.*)"""".r
  val AttrRe = """ (.+)=""".r
  val NumberRe = """[\d,]+""".r
  val PunctuationRe = """[.,?!'"]+""".r
  val CommaRe = """[,]+""".r

  case class A(target: String, username: String, tokens: Seq[String], label: String)

  def go(fn: String, out: String) {
    writeUsing(File(out)) { f =>
      for (a <- get(fn))
        f.wl(f"target=${a.target},username=${a.username},${a.tokens.map { "token=" + _ }.mkString(",")},${a.label}")
    }
  }

  def get(fn: String) = {
    File(fn).readLines
      .map(_.trim)
      .splitWhere((s: String) => s.startsWith("<item"), KeepDelimiterAsFirst)
      .filter(_.head.startsWith("<item"))
      .flatMap {
        case Seq(ItemLineRe(itemLine), ContentLineRe(contentLine), _*) =>
          val at = itemLine.split("\"").grouped(2).map(_.toVector).map {
            case Seq(AttrRe(f), v) => (f, v)
            case Seq(AttrRe(f)) => (f, "")
            case x => sys.error(itemLine + "\n" + x)
          }.toMap
          val label = at("label")
          if (Set("positive", "negative", "neutral")(label)) {
            val tokenized = Twokenize.tokenizeForTagger(contentLine)
            val cleaned =
              tokenized.flatMap {
                case CommaRe() => None
                case t @ NumberRe() => Some(t.replace(",", ""))
                case t @ PunctuationRe() => Some(t.replace(",", ""))
                case t => Some(t)
              }
            cleaned.foreach { t =>
              if (t != "," && t.contains(","))
                println(t + " --- " + contentLine)
            }
            Some(A(
              at("target").replaceAll("\\s+", "_"),
              at("username").replaceAll("\\s+", "_"),
              cleaned, label))
          }
          else None
      }
      .toVector
  }

  def verifyLexiconFile(fn: String) = {
    File(fn).readLines
      .map(_.trim)
      .dropWhile(_.startsWith(";"))
      .foreach { line =>
        if (BannedSymbols.exists(line.contains)) {
          println(line)
        }
      }
  }

  def main(args: Array[String]): Unit = {

    verifyLexiconFile("/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/opinion-lexicon/negative-words.txt")
    verifyLexiconFile("/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/opinion-lexicon/positive-words.txt")

    go(
      "/Users/dhg/temp/gpp/data/debate08/train.xml",
      "/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/debate08/train.txt")
    go(
      "/Users/dhg/temp/gpp/data/debate08/dev.xml",
      "/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/debate08/dev.txt")
    go(
      "/Users/dhg/temp/gpp/data/debate08/test.xml",
      "/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/debate08/test.txt")
    go(
      "/Users/dhg/temp/gpp/data/hcr/train.xml",
      "/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/hcr/train.txt")
    go(
      "/Users/dhg/temp/gpp/data/hcr/dev.xml",
      "/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/hcr/dev.txt")
    go(
      "/Users/dhg/temp/gpp/data/hcr/test.xml",
      "/Users/dhg/workspace/class/nlpclass-fall2013/data/classify/hcr/test.txt")

  }

}
