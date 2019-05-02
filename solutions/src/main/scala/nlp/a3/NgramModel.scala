package nlp.a3

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.math.MathUtil._
import dhg.util.Pattern._
import nlpclass.NgramModelToImplement
import nlpclass.CommandLineUtil
import nlp.a0.NGramCounting
import nlp.a0.Clean
import nlpclass.NgramModelTrainerToImplement
import nlp.a1.ConditionalProbabilityDistribution
import nlp.a1.ProbabilityDistribution
import nlpclass.Tokenize
import nlpclass.NgramModelEvaluator
import nlp.a1.ProbabilityDistribution
import com.typesafe.scalalogging.log4j.Logging
import nlpclass.CommandLineUtil
import nlpclass.Utilities._
import nlpclass._

class NgramModel(
  val n: Int,
  val ngramProbs: ConditionalProbabilityDistribution[Vector[String], String])
  extends NgramModelToImplement
  with Logging {
  require(n > 0)

  /**
   * Determine the probability of a full sentence
   *
   * USAGE: ngramModel.sentenceProb(Vector("this", "is", "a", "complete", "sentence"))
   */
  override def sentenceProb(sentenceTokens: Vector[String]): Double = {
    val endedSentence =
      if (n == 1)
        sentenceTokens
      else
        Vector.fill(n - 1)(NgramConstants.Start) ++ sentenceTokens :+ NgramConstants.End
    endedSentence
      .sliding(n)
      .map {
        case context :+ word =>
          val p = math.log(ngramProbs(word, context))
          //println(f"${s"p($word|${context.mkString(",")})"}%-20s= $p%.4f  ${math.exp(p)}%.4f")
          p
      }
      .sum
  }

  /**
   * Generate a sentence.
   *
   * Return something like: Vector("this", "is", "a", "complete", "sentence")
   */
  override def generate(): Vector[String] = {
    if (n > 1) {
      val itr = Iterator.iterate(Vector.fill(n - 1)(NgramConstants.Start)) {
        prev => prev.tail :+ ngramProbs.sample(prev)
      }
      itr.drop(1).map(_.last).takeWhile(_ != NgramConstants.End).toVector
    }
    else {
      Vector.fill(10)(ngramProbs.sample(Vector()))
    }
  }

}

class InterpolatedNgramModel(
  val n: Int,
  models: Vector[(NgramModelToImplement, Double)])
  extends NgramModelToImplement {

  val modelDist =
    new ProbabilityDistribution(
      models.zipWithIndex.map { case ((m, p), i) => (i, p) }.toMap)

  /**
   * Determine the probability of a full sentence
   *
   * USAGE: ngramModel.sentenceProb(Vector("this", "is", "a", "complete", "sentence"))
   */
  override def sentenceProb(sentenceTokens: Vector[String]): Double = {
    models.foldLeft(Double.NegativeInfinity) {
      case (z, (model, lambda)) => logadd(z, math.log(lambda) + model.sentenceProb(sentenceTokens))
    }
  }

  /**
   * Generate a sentence.
   *
   * Return something like: Vector("this", "is", "a", "complete", "sentence")
   */
  override def generate(): Vector[String] = {
    if (n > 1) {
      //      var v = Vector.fill(models.size - 1)(NgramConstants.Start)
      //      while (v.last == NgramConstants.End) {
      //        val m = models(modelDist.sample)
      //        v = v :+ m.
      //      }
      //      v.drop(n - 1).dropRight(1)
      models.maxBy(_._2)._1.generate()
    }
    else {
      models.head._1.generate()
    }
  }

}

class UnsmoothedNgramModelTrainer(val n: Int) extends NgramModelTrainerToImplement {

  override def train(tokenizedSentences: Vector[Vector[String]]): NgramModelToImplement = {
    val endedSentences =
      if (n == 1)
        tokenizedSentences
      else {
        tokenizedSentences.map(s => Vector.fill(n - 1)(NgramConstants.Start) ++ s :+ NgramConstants.End)
      }

    val cpd =
      new ConditionalProbabilityDistribution(
        endedSentences
          .flatMap(_.sliding(n))
          .map { case context :+ word => (context, word) }
          .groupByKey
          .mapVals(words => new ProbabilityDistribution(words.counts.mapVals(_.toDouble))))
    new NgramModel(n, cpd)
  }

}

class AddLambdaNgramModelTrainer(val n: Int, val lambda: Double) extends NgramModelTrainerToImplement with Logging {

  override def train(tokenizedSentences: Vector[Vector[String]]): NgramModelToImplement = {
    val vocab =
      if (n == 1)
        tokenizedSentences.flatten.toSet
      else
        tokenizedSentences.flatten.toSet + NgramConstants.End

    val endedSentences =
      if (n == 1)
        tokenizedSentences
      else
        tokenizedSentences.map(s => Vector.fill(n - 1)(NgramConstants.Start) ++ s :+ NgramConstants.End)

    val cpd =
      new ConditionalProbabilityDistribution(
        endedSentences
          .flatMap(_.sliding(n))
          .map { case context :+ word => (context, word) }
          .groupByKey
          .map {
            case (context, words) =>
              val counts = words.counts
              //logger.debug(f"[${context.mkString(" ")}]: ${counts.toVector.sortBy(_._1).map { case (w, c) => f"$w -> $c" }.mkString(", ")}")
              val excluded =
                if (context.lastOption.map(_ == NgramConstants.Start).getOrElse(true))
                  Some(Set(NgramConstants.End)) // END can't directly follow START
                else
                  None
              context -> new ProbabilityDistribution(counts.mapVals(_.toDouble), Some(vocab), excluded, lambda)
          },
        ???,
        ???,
        new ProbabilityDistribution(Map(), Some(vocab), None, lambda))
    new NgramModel(n, cpd)
  }

}

/**
 *
 */
class InterpolatedNgramModelTrainer(n: Int, lambda: Double) extends NgramModelTrainerToImplement with Logging {

  /**
   * Pairs of (trainer, proportion), where:
   *   - `trainer` is an add-lambda trainer
   *   - `proportion` is the weight given to the model produced by `trainer` in the
   *     final interpolated model.  The weights are calculated as 2^(n-1), which means
   *     that the weight will double as `n` increases.  So if we are training an
   *     interpolated trigram model, the three sub-models will have proportions:
   *       n=1: 1/7 = 0.14
   *       n=2: 2/7 = 0.29
   *       n=3: 4/7 = 0.57
   */
  def interpParams =
    (n to 1 by -1)
      .map(n => (new AddLambdaNgramModelTrainer(n, lambda), math.pow(2, n - 1)))
      .normalizeValues
      .toVector

  override def train(tokenizedSentences: Vector[Vector[String]]): NgramModelToImplement = {
    val models =
      interpParams.map {
        case (trainer, interp) => (trainer.train(tokenizedSentences), interp)
      }
    new InterpolatedNgramModel(n, models)
  }

}

object PerplexityNgramModelEvaluator extends NgramModelEvaluator with Logging {

  override def apply(model: NgramModelToImplement, tokenizedSentences: Vector[Vector[String]]): Double = {
    val totalTokenCount = tokenizedSentences.map(_.size).sum
    val ps = tokenizedSentences.map { sent =>
      val p = model.sentenceProb(sent)
      //logger.info(f"$p%.4f  ${sent.mkString(" ")}")
      p
    }

    math.exp(-ps.sum / totalTokenCount)
  }

}

object NgramConstants {
  val Start = "<S>"
  val End = "<E>"
}

object Ngrams {

  def fileTokens(filename: String) = {
    File(filename).readLines
      .split("")
      .flatMap(paragraph => Tokenize(paragraph.mkString(" ")))
      .map(_.map(_.toLowerCase))
      .toVector
  }

  def main(args: Array[String]): Unit = {

    val (arguments, options) = CommandLineUtil.parseArgs(args)

    val n = options("n").toInt

    val trainer =
      if (options.get("interp").map(_.toBoolean).getOrElse(false)) {
        new InterpolatedNgramModelTrainer(n,
          options.get("lambda").map(_.toDouble).getOrElse(0.0))
      }
      else {
        options.get("lambda").map(_.toDouble).map { lambda =>
          new AddLambdaNgramModelTrainer(n, lambda)
        }.getOrElse {
          new UnsmoothedNgramModelTrainer(n)
        }
      }

    val trainData = fileTokens(options("train"))
    val model = trainer.train(trainData)

    val testData = fileTokens(options("test"))
    println(PerplexityNgramModelEvaluator(model, testData))
  }

}
