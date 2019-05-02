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

object MaxEnt {

  def main(args: Array[String]): Unit = {
    val (arguments, options) = CommandLineUtil.parseArgs(args)

    val trainInstances = CountFeatures.readInstancesFromFile(options("train"))

    val featureExtender = NaiveBayes.getFeatureExtender(options.getOrElse("extend", "none"), options)

    val trainer = new MaxEntModelTrainer(featureExtender, options.get("sigma").fold(1.0)(_.toDouble))
    val model = trainer.train(trainInstances)

    val testInstances = CountFeatures.readInstancesFromFile(options("test"))
    ClassifierScorer.score(model, testInstances)

  }

}
