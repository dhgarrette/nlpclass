package nlp.a0

import dhg.util.FileUtil._
import dhg.util.CollectionUtil._
import scalaz._
import scalaz.Scalaz._
import nlpclass.CommandLineUtil

object WordCount {

  /**
   * Count using mutable maps -- procedural style
   */
  def countWordsMutableMap(textFile: String): Map[String, Int] = {
    val counts = collection.mutable.Map[String, Int]() // make a mutable map
    for (token <- Clean.splitIntoWords(File(textFile).readLines.mkString(" "))) { // make the file into one big string, split, iterate
      val existingCount = counts.getOrElse(token, 0) // get any existing counts, or zero if the word has not been seen yet
      counts(token) = existingCount + 1 // add one to the existing count (or zero), store it back to the map
    }
    counts.toMap // convert to an immutable map for returning
  }

  /**
   * Count using only built-in Scala functional features
   */
  def countWordsFunctional(textFile: String): Map[String, Int] = {
    File(textFile).readLines
      .flatMap(line => Clean.splitIntoWords(line))
      .groupBy(word => word) // group token occurrences by what word they are
      .map { case (word, occurrences) => word -> occurrences.size } // count the number of occurrences
  }

  /**
   * Count using my functional features and scalaz.  Get counts for each line
   * individually and then reduce them to one Map of counts.
   */
  def countWordsFunctionalIterator(textFile: String): Map[String, Int] = {
    File(textFile).readLines
      .map(Clean.splitIntoWords(_).counts) // split and count each line individually
      .reduce(_ |+| _) // reduce all the individual count maps into one complete counts map
  }

  def main(args: Array[String]): Unit = {

    val (arguments, options) = CommandLineUtil.parseArgs(args)

    val Vector(textFile) = arguments // pattern match for exactly one argument
    val counts = countWordsFunctionalIterator(textFile)

    val totalWords = counts.values.sum

    val stopwords =
      options
        .get("stopwords") // get the stopwords option, if it is present
        .map(filename => File(filename).readLines.flatMap(Clean.splitIntoWords).toSet) // process each line, flatten, and turn it into a set
        .getOrElse(Set.empty) // if no stopwords file was given, then stopwords defaults to the empty set

    println(s"Total number of words: $totalWords")
    println(s"Number of distinct words: ${counts.size}")
    println("Top 10 words:")

    val countsWithoutStops = counts -- stopwords // remove any stopwords from the map

    countsWithoutStops
      .toVector // convert Map to Vector for sorting
      .sortBy(-_._2) // reverse sort by the count
      .take(10) // take the top 10
      .foreach {
        case (w, c) => println(f"$w%-14s $c%-7s ${c * 100.0 / totalWords}%.2f")
      }
  }

}

object Clean {

  def splitIntoWords(s: String) = s
    .toLowerCase
    .split("[^a-z]") // split on non-alpha characters
    .filter(_.nonEmpty) // remove any empty strings
    .toVector

}

