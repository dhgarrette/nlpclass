package nlp.a5

import dhg.util.FileUtil._
import dhg.util.CollectionUtil._
import dhg.util.StringUtil._
import dhg.util.Pattern._
import dhg.util.Time._
import dhg.util.CommandLineUtil._
import java.io.FileReader
import nlpclass._
import nlp.a0.CountFeatures
import nlp.a2._

class LexiconRatioSentimentClassifier(
  positiveWords: Set[String],
  negativeWords: Set[String])
  extends Classifier[String, String, String] {

  private[this]type Feature = String
  private[this]type Label = String
  private[this]type Value = String

  def predict(features: Vector[(Feature, Value)]): Label = {
    var pos = 0
    var neg = 0
    for (("token", v) <- features) {
      val lcv = v.toLowerCase
      if (positiveWords(lcv))
        pos += 1
      else if (negativeWords(lcv))
        neg += 1
    }
    if (pos > neg)
      "positive"
    else if (pos < neg)
      "negative"
    else
      "neutral"
  }

}

//class LexiconRatioSentimentClassifierTrainer[Label, Feature, Value](
//  positiveWords: Set[String],
//  negativeWords: Set[String])
//  extends ClassifierTrainer[Label, Feature, Value] {
//
//  def train(instances: Vector[(Label, Vector[(Feature, Value)])]): Classifier[Label, Feature, Value] = {
//    ???
//  }
//
//}

object SentimentLexiconRatio {

  def main(args: Array[String]): Unit = {
    val (arguments, options) = CommandLineUtil.parseArgs(args)

    def readPolFile(fn: String) = File(fn).readLines.dropWhile(_.startsWith(";")).toSet

    val p = readPolFile(options("pos"))
    val n = readPolFile(options("neg"))

    val model = new LexiconRatioSentimentClassifier(p, n)

    val testInstances = CountFeatures.readInstancesFromFile(options("test"))
    ClassifierScorer.score(model, testInstances)

  }

}
