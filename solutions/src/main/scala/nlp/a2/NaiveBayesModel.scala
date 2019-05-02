package nlp.a2

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil._
import nlp.a1.ConditionalProbabilityDistribution
import nlp.a1.ProbabilityDistribution
import nlpclass.ProbabilityDistributionToImplement
import nlpclass.ConditionalProbabilityDistributionToImplement
import nlp.a1.FeatureFileAsDistributions
import nlp.a0.CountFeatures
import com.typesafe.scalalogging.log4j.Logging
import scala.util.Random
import nlpclass.FeatureExtender
import nlpclass.NoOpFeatureExtender
import nlpclass.CompositeFeatureExtender
import nlpclass.Lemmatize
import dhg.util.Pattern
import nlpclass.CommandLineUtil
import nlpclass.Classifier
import nlpclass.ClassifierTrainer
import nlpclass.ClassifierScorerToImplement

class NaiveBayesModel[Label, Feature, Value](
  labels: Set[Label],
  pLabel: ProbabilityDistributionToImplement[Label],
  pValue: Map[Feature, ConditionalProbabilityDistributionToImplement[Label, Value]],
  featureExtender: FeatureExtender[Feature, Value] = new NoOpFeatureExtender[Feature, Value])
  extends Classifier[Label, Feature, Value]
  with Logging {

  def predict(features: Vector[(Feature, Value)]): Label = {
    val posteriors =
      labels.mapTo { label =>
        val p = pLabel(label) * featureExtender(features).map { case (f, v) => pValue(f)(v, label) }.product
        //println(f"$p%.4f  p($label)=${pLabel(label)}%.4f ${featureExtender(features).map { case (f, v) => f"p($f=$v|$label)=${pValue(f)(v, label)}%.4f" }.mkString(" ")}")
        p
      }

    val maxPriorLabel = labels.mapTo(pLabel(_)).maxBy(-_._2)._1

    if (posteriors.forall(_._2 == 0.0)) {
      logger.info("All posteriors are zero!")
      maxPriorLabel
    }
    else {
      val sorted = posteriors.toVector.sortBy(-_._2)
      logger.info(sorted.normalizeValues.map { case (l, p) => f"$l%-4s $p%.4f" }.mkString("  "))
      sorted.head._1
    }
  }

}

class LogNaiveBayesModel[Label, Feature, Value](
  labels: Set[Label],
  pLabel: ProbabilityDistributionToImplement[Label],
  pValue: Map[Feature, ConditionalProbabilityDistributionToImplement[Label, Value]],
  featureExtender: FeatureExtender[Feature, Value] = new NoOpFeatureExtender[Feature, Value])
  extends Classifier[Label, Feature, Value]
  with Logging {

  def predict(features: Vector[(Feature, Value)]): Label = {
    val logPosteriors =
      labels.mapTo { label =>
        math.log(pLabel(label)) + featureExtender(features).map { case (f, v) => math.log(pValue(f)(v, label)) }.sum
      }

    if (logPosteriors.forall(_._2 == Double.NegativeInfinity)) {
      logger.info("All posteriors are zero!")
      labels.mapTo(pLabel(_)).maxBy(-_._2)._1
    }
    else {
      val logSorted = logPosteriors.toVector.sortBy(-_._2)
      val (maxLabel, maxLogP) = logSorted.head
      val logNormed = logSorted.mapVals(lp => math.exp(lp - maxLogP)).normalizeValues
      //      println(f"logSorted = $logSorted")
      //      println(f"logNormed = $logNormed")
      logger.info(logNormed.map { case (l, p) => f"$l%-4s $p%.4f" }.mkString("  "))
      maxLabel
    }
  }

}

class UnsmoothedNaiveBayesTrainer[Label, Feature, Value](
  featureExtender: FeatureExtender[Feature, Value] = new NoOpFeatureExtender[Feature, Value])
  extends ClassifierTrainer[Label, Feature, Value] {
  def train(instances: Vector[(Label, Vector[(Feature, Value)])]): Classifier[Label, Feature, Value] = {
    val (labels, pLabel, pValue) = FeatureFileAsDistributions.fromInstances(instances.mapVals(featureExtender))
    new NaiveBayesModel(labels, pLabel, pValue, featureExtender)
  }
}

class AddLambdaNaiveBayesTrainer[Label, Feature, Value](
  lambda: Double,
  featureExtender: FeatureExtender[Feature, Value] = new NoOpFeatureExtender[Feature, Value])
  extends ClassifierTrainer[Label, Feature, Value] {

  def train(instances: Vector[(Label, Vector[(Feature, Value)])]): Classifier[Label, Feature, Value] = {
    val extendedInstances = instances.mapVals(featureExtender)
    //extendedInstances.foreach(println)
    val (labels, labelCounts, valueCounts) = CountFeatures.featureCounts(extendedInstances)

    val values = valueCounts.mapVals(_.flatMap(_._2.keys).toSet)

    val pValue =
      valueCounts.map {
        case (feature, conditionedCounts) =>
          feature ->
            new ConditionalProbabilityDistribution(
              conditionedCounts.mapVals(counts =>
                new ProbabilityDistribution(counts.mapVals(_.toDouble), Some(values(feature)), None, lambda)))
      }

    make(labels,
      new ProbabilityDistribution(labelCounts.mapVals(_.toDouble)),
      pValue)
  }

  protected def make(
    labels: Set[Label],
    pLabel: ProbabilityDistributionToImplement[Label],
    pValue: Map[Feature, ConditionalProbabilityDistributionToImplement[Label, Value]]): Classifier[Label, Feature, Value] = {
    new NaiveBayesModel(labels, pLabel, pValue, featureExtender)
  }
}

class LogAddLambdaNaiveBayesTrainer[Label, Feature, Value](
  lambda: Double,
  featureExtender: FeatureExtender[Feature, Value] = new NoOpFeatureExtender[Feature, Value])
  extends AddLambdaNaiveBayesTrainer[Label, Feature, Value](lambda, featureExtender) {

  override protected def make(
    labels: Set[Label],
    pLabel: ProbabilityDistributionToImplement[Label],
    pValue: Map[Feature, ConditionalProbabilityDistributionToImplement[Label, Value]]) = {
    new LogNaiveBayesModel(labels, pLabel, pValue, featureExtender)
  }
}

trait PpaFeatureExtender extends FeatureExtender[String, String] {
  val features = Set("verb", "noun", "prep", "prep_obj")
  object IsFeat {
    def unapply(f: String): Boolean = features(f)
  }
}

class PpaNumberFeatureExtender extends PpaFeatureExtender {
  val NumberRe = Pattern.UDouble.DoubleRE
  override def extendFeatures(features: Vector[(String, String)]) = {
    features.flatMap {
      case t @ (f @ IsFeat(), word) => Vector(t, (f + "_isnum", NumberRe.pattern.matcher(word).matches.toString))
      case t => Vector(t)
    }
  }
}

class PpaComboFeatureExtender(feats: Vector[String]) extends PpaFeatureExtender {
  val NumberRe = Pattern.UDouble.DoubleRE
  override def extendFeatures(features: Vector[(String, String)]) = {
    val values = features.toMap
    features :+ (feats.mkString("+") -> feats.flatMap(values.get).mkString("+"))
  }
}

class PpaLemmaFeatureExtender(replace: Boolean = false) extends PpaFeatureExtender {
  override def extendFeatures(features: Vector[(String, String)]) = {
    features.flatMap {
      case t @ (f @ IsFeat(), word) => (if (replace) None else Some(t)) ++ Vector(f + "_lemma" -> Lemmatize(word))
      case t => Vector(t)
    }
  }
}

class PpaWordShapeFeatureExtender extends PpaFeatureExtender {
  override def extendFeatures(features: Vector[(String, String)]) = {
    features.flatMap {
      case t @ (f @ IsFeat(), word) => Vector(t, (f + "_wordshape", {
        word.flatMap {
          case c if 'A' <= c && c <= 'Z' => Some('X')
          case c if 'a' <= c && c <= 'z' => Some('x')
          case c if '0' <= c && c <= '9' => Some('N')
          case _ => None
        }.take(3)
      }))
      case t => Vector(t)
    }
  }
}

class PpaClusterFeatureExtender(clusters: Map[String, String]) extends PpaFeatureExtender {
  override def extendFeatures(features: Vector[(String, String)]) = {
    features.flatMap {
      case t @ (f @ IsFeat(), word) => Vector(t) ++ clusters.get(word).map(cl => (f + "_cl", cl))
      case t => Vector(t)
    }
  }
}

object NaiveBayes {

  def getFeatureExtender(option: String, options: Map[String, String]) = {
    option match {
      case "ppa" =>
        new CompositeFeatureExtender[String, String](
          (options.get("clusters").map(fn => new PpaClusterFeatureExtender(File(fn).readLines.map(_.split("\\s+").toTuple2).toMap)) ++
            Vector(
              new PpaNumberFeatureExtender(),
              new PpaLemmaFeatureExtender(replace = false),
              new PpaWordShapeFeatureExtender(),
              new PpaComboFeatureExtender(Vector("noun", "verb")),
              new PpaComboFeatureExtender(Vector("verb", "prep")),
              new PpaComboFeatureExtender(Vector("prep", "prep_obj")))).toVector)
      case "hcr" =>
        new NoOpFeatureExtender[String, String]
      case "none" =>
        new NoOpFeatureExtender[String, String]
    }
  }

  def main(args: Array[String]): Unit = {
    val (arguments, options) = CommandLineUtil.parseArgs(args)

    val trainInstances = CountFeatures.readInstancesFromFile(options("train"))

    val featureExtender = getFeatureExtender(options.getOrElse("extend", "none"), options)

    val trainer =
      if (options.get("log").map(_.toBoolean).getOrElse(false)) {
        new LogAddLambdaNaiveBayesTrainer[String, String, String](
          options.get("lambda").map(_.toDouble).getOrElse(0.0),
          featureExtender)
      }
      else {
        options.get("lambda").map { lambda =>
          new AddLambdaNaiveBayesTrainer[String, String, String](lambda.toDouble, featureExtender)
        }.getOrElse(new UnsmoothedNaiveBayesTrainer[String, String, String](featureExtender))
      }
    val model = trainer.train(trainInstances)

    val testInstances = CountFeatures.readInstancesFromFile(options("test"))
    ClassifierScorer.score(model, testInstances)

  }
}
