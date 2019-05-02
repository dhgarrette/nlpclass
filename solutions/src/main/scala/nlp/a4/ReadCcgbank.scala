package nlp.a4

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil._
import com.typesafe.scalalogging.log4j.Logging
import scalaz._
import Scalaz._

object ReadCcgbank {

  val Loc = "ccgbank"

  val outputs = Map(
    "td" -> (0 to 15),
    "raw" -> (16 to 18),
    "train" -> (0 to 18),
    "dev" -> (19 to 21),
    "test" -> (22 to 24))

  val IdLine = """ID=.+""".r
  // (<L N/N NNP NNP Pierre N_73/N_73>)
  val Terminal = """\(<L (\S+) (\S+) (\S+) (\S+) \S+>\)""".r

  def main(args: Array[String]) = {

    for (tagType <- Vector("ccgpos", "supertag", "supertagfeat")) {
      val sections =
        File(Loc).ls("\\d+".r).map {
          section =>
            section.name.toInt ->
              (for (
                f <- section.ls.view;
                Seq(IdLine(), line) <- f.readLines.sliding(2)
              ) yield {
                Terminal.allGroups(line).map {
                  case Seq(cat, pos, _, word) => (word,
                    tagType match {
                      case "ccgpos" => pos
                      case "supertag" => cat.replaceAll("""\[[^\[\]]+\]""", "") // Remove features
                      case "supertagfeat" => cat // Keep features
                    })
                }.toVector
              })
        }.toMap

      for ((cut, sectionNums) <- outputs) {
        writeUsing(File(tagType, cut + ".txt")) { w =>
          for (
            section <- sectionNums.map(sections);
            sentence <- section
          ) {
            w.wl(sentence.map {
              case (w, t) =>
                assert(!w.contains("|"))
                assert(!t.contains("|"))
                f"$w|$t"
            }.mkString(" "))
          }
        }
      }
    }

  }

}
