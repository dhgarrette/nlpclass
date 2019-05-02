package nlp.a0

import dhg.util.FileUtil._
import dhg.util.CollectionUtil._
import scalaz._
import scalaz.Scalaz._
import nlpclass.NGramCountingToImplement
import nlpclass.CommandLineUtil

class NGramCounting(n: Int) extends NGramCountingToImplement {
  override def countNGrams(tokens: Vector[String]): Map[Vector[String], Int] = {
    tokens.sliding(n).counts
  }
}

object CountTrigrams {

  def main(args: Array[String]): Unit = {

    val (arguments, options) = CommandLineUtil.parseArgs(args)
    val Seq(textFile) = arguments

    val counting = new NGramCounting(3) // create the counting object

    val tokens = File(textFile).readLines.flatMap(Clean.splitIntoWords).toVector // get all the tokens
    val counts = counting.countNGrams(tokens)

    for ((ngram, count) <- counts.toVector.sortBy(-_._2).take(10)) {
      println(f"${ngram.mkString(" ")}%-31s $count")
    }

    //

    val distinct = counts.size
    val countCounts = counts.map(_._2).counts
    println
    println(s"distinct trigrams = $distinct")
    val once = countCounts(1)
    println(f"appear once = $once (${once * 100.0 / distinct}%.2f)")
    val lessThan13 = countCounts.filter(_._1 <= 12).values.sum
    println(f"appear once = ${lessThan13} (${lessThan13 * 100.0 / distinct}%.2f)")

  }

}
