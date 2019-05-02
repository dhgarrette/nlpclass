package nlp.a2

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import nlp.a1.FeatureFileAsDistributions
import nlp.a0.CountFeatures
import nlpclass.CommandLineUtil

object A2Check {

  def main(args: Array[String]): Unit = {

    val (arguments, options) = CommandLineUtil.parseArgs(args)
    val Vector(filename) = arguments // pattern match for exactly one argument

    //

    {
      val (labels, pLabel, pValue) = FeatureFileAsDistributions.fromFile(filename)

      val nbm = new NaiveBayesModel(labels, pLabel, pValue)
      println(nbm.predict(Vector("Outlook" -> "Sunny", "Temperature" -> "Cool", "Humidity" -> "High", "Wind" -> "Strong"))) // No
      println(nbm.predict(Vector("Outlook" -> "Overcast", "Temperature" -> "Cool", "Humidity" -> "Normal", "Wind" -> "Weak"))) // Yes
    }

    //

    {
      val instances = CountFeatures.readInstancesFromFile(filename)
      val nbt = new UnsmoothedNaiveBayesTrainer[String, String, String]()
      val nbm = nbt.train(instances)
      println(nbm.predict(Vector("Outlook" -> "Sunny", "Temperature" -> "Cool", "Humidity" -> "High", "Wind" -> "Strong"))) // No
      println(nbm.predict(Vector("Outlook" -> "Overcast", "Temperature" -> "Cool", "Humidity" -> "Normal", "Wind" -> "Weak"))) // Yes
    }

    //

    {
      val trainInstances = CountFeatures.readInstancesFromFile("tennis/train.txt")
      val nbt = new UnsmoothedNaiveBayesTrainer[String, String, String]()
      val nbm = nbt.train(trainInstances)
      val testInstances = CountFeatures.readInstancesFromFile("tennis/test.txt")
      ClassifierScorer.score(nbm, testInstances)
      //		accuracy = 61.54
      //		precision (Yes) = 66.67
      //		recall (Yes) = 75.00
      //		f1 = 70.59
      ClassifierScorer.score(nbm, testInstances)
      //		accuracy = 61.54
      //		precision (No) = 50.00
      //		recall (No) = 40.00
      //		f1 = 44.44
    }

    //

    {
      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "Yes"))
      //        accuracy = 61.54
      //		precision (Yes) = 66.67
      //		recall (Yes) = 75.00
      //		f1 = 70.59

      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "No"))
      //      accuracy = 61.54
      //      precision(No) = 50.00
      //      recall(No) = 40.00
      //      f1 = 44.44
    }

    //

    {
      val instances = CountFeatures.readInstancesFromFile(filename)
      val nbt = new AddLambdaNaiveBayesTrainer[String, String, String](2.0)
      val nbm = nbt.train(instances)
      println(nbm.predict(Vector("Outlook" -> "Overcast", "Temperature" -> "Hot", "Humidity" -> "High", "Wind" -> "Strong"))) // Yes
    }

    {
      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "Yes",
        "--lambda", "1.0"))
      //        accuracy = 61.54
      //		precision (Yes) = 66.67
      //		recall (Yes) = 75.00
      //		f1 = 70.59

      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "Yes",
        "--lambda", "4.0"))
      //		accuracy = 76.92
      //		precision (Yes) = 72.73
      //		recall (Yes) = 100.00
      //		f1 = 84.21
    }

    {
      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "Yes",
        "--lambda", "0.0",
        "--log", "true"))
      //        accuracy = 61.54
      //		precision (Yes) = 66.67
      //		recall (Yes) = 75.00
      //		f1 = 70.59

      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "Yes",
        "--lambda", "1.0",
        "--log", "true"))
      //        accuracy = 61.54
      //		precision (Yes) = 66.67
      //		recall (Yes) = 75.00
      //		f1 = 70.59

      NaiveBayes.main(Array(
        "--train", "tennis/train.txt",
        "--test", "tennis/test.txt",
        "--poslab", "Yes",
        "--lambda", "4.0",
        "--log", "true"))
      //		accuracy = 76.92
      //		precision (Yes) = 72.73
      //		recall (Yes) = 100.00
      //		f1 = 84.21
    }

    {
      NaiveBayes.main(Array(
        "--train", "ppa/train.txt",
        "--test", "ppa/dev.txt",
        "--poslab", "N",
        "--lambda", "1.0"))
      //      accuracy = 80.76
      //      precision(N) = 82.70
      //      recall(N) = 80.58
      //      f1 = 81.63

    }

  }
}
