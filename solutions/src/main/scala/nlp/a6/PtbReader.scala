package nlp.a6

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.Pattern._
import dhg.util.StringUtil._
import nlpclass._
import nlp.a1.ConditionalProbabilityDistribution
import nlp.a1.ProbabilityDistribution
import com.typesafe.scalalogging.log4j.Logging
import nlpclass.CommandLineUtil
import scala.math.{ log, exp }
import scalaz._
import Scalaz._
import dhg.util.math.LogDouble
import scala.util.Random
import nlpclass.Tree

object PtbReader {

  val FilenameRe = "wsj_(\\d{2})(\\d{2}).mrg".r

  def readPtb(ptbLoc: String) = {
    File(ptbLoc).ls.toVector
      .flatMap { f =>
        f.name match {
          case FilenameRe(UInt(section), _) => Some((section, f))
          case _ => None
        }
      }
      .groupByKey
      .toVector
      .sortBy { case (section, files) => section }
      .map {
        case (section, files) =>
          (for (
            file <- files.sortBy(_.name).iterator;
            treelines <- file.readLines.filter(_.trim.nonEmpty).splitWhere((s: String) => s.startsWith("("), KeepDelimiter.KeepDelimiterAsFirst) if treelines.nonEmpty
          ) yield {
            Tree.fromString(treelines.mkString(" "))
          })
      }.toVector
  }

  def ptbToPreppedFile(ptbLoc: String, trainRange: Range, devRange: Range, testRange: Range) = {
    /*
	 * sections -> sentences
	 */
    val sections = readPtb(ptbLoc)
    for (
      (fn, range) <- Vector(
        ("train", trainRange),
        ("dev", devRange),
        ("test", testRange))
    ) {
      writeUsing(File("parsed", fn + ".txt")) { f =>
        for (
          section <- sections.slice(range.head, range.last);
          sentence <- section
        ) {
          f.writeLine(sentence)
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    ptbToPreppedFile("/Users/dhg/Corpora/ptb3/combined", 0 to 18, 19 to 21, 22 to 24)
  }

}
