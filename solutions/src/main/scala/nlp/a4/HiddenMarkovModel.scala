package nlp.a4

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil._
import math.{ log, exp }
import nlpclass._
import nlp.a1.ConditionalProbabilityDistribution
import com.typesafe.scalalogging.log4j.Logging
import nlp.a1.ProbabilityDistribution
import nlp.a1.ConditionalProbabilityDistribution
import scalaz._
import Scalaz._
import dhg.util.Time

class HiddenMarkovModel[Word, Tag](
  val transitions: ConditionalProbabilityDistribution[Tag, Tag],
  val emissions: ConditionalProbabilityDistribution[Tag, Word],
  val tagdict: TagDictionary[Word, Tag])
  extends Tagger[Word, Tag] with Logging {

  /**
   * Compute the probability of the tagged sentence.  The result
   * should be represented as a logarithm.
   */
  override def sentenceProb(sentence: Vector[(Word, Tag)]): Double = {
    ((tagdict.startWord -> tagdict.startTag) +: sentence :+ (tagdict.endWord -> tagdict.endTag))
      .sliding2.foldLeft(log(1.0)) {
        case (logProd, ((_, prevTag), (currWord, currTag))) =>
          logProd + log(transitions(currTag, prevTag)) + log(emissions(currWord, currTag))
      }
  }

  /**
   * Accepts a sentence of word tokens and returns a sequence of
   * tags corresponding to each of those words.
   */
  override def tagSentence(sentence: Vector[Word]): Vector[Tag] = {
    val forwards =
      (sentence :+ tagdict.endWord).scanLeft(Map(tagdict.startTag -> (log(1.0), tagdict.startTag))) {
        case (prevV, currWord) =>
          logger.debug(f"currWord = $currWord")
          logger.debug(f"  prev viterbi = $prevV")
          val potentialTags = tagdict(currWord)
          val v =
            potentialTags.mapTo { k =>
              logger.debug(f"  k=$k")
              val scores =
                prevV.map {
                  case (kprime, (kprimeScore, _)) =>
                    val score = kprimeScore + log(transitions(k, kprime))
                    //if (currWord == "the" && kprime == "V" && k == "D") { // TODO: REMOVE
                    logger.debug(f"    $currWord  $kprime%-3s -> $k%-3s  $score")
                    logger.debug(f"      v($kprime) = ${kprimeScore}")
                    logger.debug(f"      tr($kprime -> $k) = ${log(transitions(k, kprime))}")
                    //}
                    kprime -> score
                }
              val (bestKprime, bestKprimeScore) = scores.maxBy(_._2)
              //if ((currWord == "the" && k == "D") || currWord == "held") { // TODO: REMOVE
              logger.debug(f"    scores: $scores -> $k")
              logger.debug(f"    best: $bestKprime%-3s -> $k%-3s  bestKprimeScore=$bestKprimeScore")
              logger.debug(f"    em($currWord | $k) = ${emissions(currWord, k)}")
              //}
              (log(emissions(currWord, k)) + bestKprimeScore, bestKprime)
            }.toMap
          logger.debug(f"  v = $v")
          v
      }
    forwards.scanRight(tagdict.endTag) {
      (v, kNext) => v(kNext)._2
    }.drop(2).dropRight(1) // drop start/end tags
  }

}

abstract class AbstractHmmTrainer[Word, Tag](startWord: Word, startTag: Tag, endWord: Word, endTag: Tag, tdCutoff: Option[Double]) extends TaggerTrainer[Word, Tag] {
  def getTagdict(taggedSentences: Vector[Vector[(Word, Tag)]]): TagDictionary[Word, Tag] = {
    val tdFactory = new SimpleTagDictionaryFactory(tdCutoff)
    tdFactory(taggedSentences, startWord, startTag, endWord, endTag)
  }
}

class UnsmoothedHmmTrainer[Word, Tag](
  startWord: Word, startTag: Tag, endWord: Word, endTag: Tag,
  tdCutoff: Option[Double] = None)
  extends AbstractHmmTrainer[Word, Tag](startWord, startTag, endWord, endTag, tdCutoff) {

  override def train(taggedSentences: Vector[Vector[(Word, Tag)]]): Tagger[Word, Tag] = {
    val tagdict = getTagdict(taggedSentences)
    val transitions = new UnsmoothedTransitionDistributioner()(taggedSentences, tagdict)
    val emissions = new UnsmoothedEmissionDistributioner()(taggedSentences, tagdict)
    new HiddenMarkovModel(transitions, emissions, tagdict)
  }
}

class AddLambdaSmoothedHmmTrainer[Word, Tag](
  lambda: Double,
  startWord: Word, startTag: Tag, endWord: Word, endTag: Tag,
  tdCutoff: Option[Double])
  extends AbstractHmmTrainer[Word, Tag](startWord, startTag, endWord, endTag, tdCutoff) {

  override def train(taggedSentences: Vector[Vector[(Word, Tag)]]): Tagger[Word, Tag] = {
    val tagdict = getTagdict(taggedSentences)
    val transitions = new AddLambdaTransitionDistributioner(lambda)(taggedSentences, tagdict)
    val emissions = new AddLambdaEmissionDistributioner(lambda)(taggedSentences, tagdict)
    new HiddenMarkovModel(transitions, emissions, tagdict)
  }
}

class SmoothedHmmTrainer[Word, Tag](
  transitionDister: TransitionDistributioner[Word, Tag], emissionDister: EmissionDistributioner[Word, Tag],
  startWord: Word, startTag: Tag, endWord: Word, endTag: Tag,
  tdCutoff: Option[Double])
  extends AbstractHmmTrainer[Word, Tag](startWord, startTag, endWord, endTag, tdCutoff) {

  override def train(taggedSentences: Vector[Vector[(Word, Tag)]]): Tagger[Word, Tag] = {
    val tagdict = getTagdict(taggedSentences)

    val transitions = transitionDister(taggedSentences, tagdict)
    val emissions = emissionDister(taggedSentences, tagdict)

    //    println("\nTRANSITIONS\n")
    //    for (t1 <- (allTags + startTag + endTag)) {
    //      for (t2 <- ((allTags + startTag + endTag)).toVector.sortBy(t2 => -transitions(t2, t1))) {
    //        println(f"$t1%-5s -> $t2%-10s  ${transitions(t2, t1)}%.2f  ${log(transitions(t2, t1))}%.2f")
    //      }
    //      println
    //    }

    //    println("\nEMISSIONS\n")
    //    for (t <- (allTags + startTag + endTag)) {
    //      for (w <- ((allWords + startWord + endWord) + "<DEFAULT>".asInstanceOf[Word]).toVector.sortBy(w => -emissions(w, t))) {
    //        println(f"$t%-5s -> $w%-10s  ${emissions(w, t)}%.2f  ${log(emissions(w, t))}%.2f")
    //      }
    //      println
    //    }

    new HiddenMarkovModel(transitions, emissions, tagdict)
  }
}

object TaggerEvaluator extends Logging {

  def apply[Word, Tag](model: Tagger[Word, Tag], testData: Vector[Vector[(Word, Tag)]]): Double = {
    var correct = 0
    var total = 0
    val errors = collection.mutable.Map[(Tag, Tag), Int]() // (gold, model) -> count
    var totalTime = 0L
    Time.time("testing", {
      for ((goldTaggedSentence, i) <- testData.zipWithIndex) {
        val tokens = goldTaggedSentence.map(_._1)
        val startTime = System.currentTimeMillis()
        val modelTagged = model.tagSentence(tokens)
        totalTime += (System.currentTimeMillis() - startTime)
        //if ((i + 1) % 100 == 0) println(f"${i + 1}%5s, total time: ${totalTime / 1000.0}%.3f sec, avg time: ${totalTime / 1000.0 / (i + 1)}%.4f sec")
        //println(tokens zipSafe modelTagged)
        logger.debug((tokens zipSafe modelTagged).map { case (w, t) => f"$w|$t" }.mkString(" "))
        for (((gw, gt), mt) <- goldTaggedSentence zipSafe modelTagged) {
          if (gt == mt) {
            correct += 1
          }
          else {
            // Error breakdown
            errors((gt, mt)) = errors.getOrElse((gt, mt), 0) + 1
          }
          total += 1
        }
      }
    })
    val accuracy = correct * 100.0 / total
    println(f"Accuracy: ${accuracy}%.2f  ($correct/$total)")
    val errorsToShow = errors.toVector.sortBy(-_._2).take(10).map { case ((gt, mt), count) => (count.toString, gt.toString, mt.toString) }
    val maxCountWidth = errorsToShow.map(_._1.length).max max 5
    val maxTagWidth = errorsToShow.map { case (_, gt, mt) => gt.size }.max
    println(f" ${"count".padLeft(maxCountWidth)}  ${"gold".padRight(maxTagWidth)}  ${"model"}")
    for ((count, gt, mt) <- errorsToShow) {
      println(f" ${count.padLeft(maxCountWidth)}  ${gt.padRight(maxTagWidth)}  ${mt}")
    }
    println(f"avg tagging: ${totalTime / 1000.0 / testData.size}%.4f sec")
    accuracy
  }
}

object Hmm extends Logging {

  /** Returns a vector of tagged sentences */
  def taggedSentencesFile(filename: String) = {
    File(filename).readLines.zipWithIndex.map {
      case (line, lineNum) =>
        line.split("\\s+")
          .map(_.split("\\|"))
          .map {
            case Array(w, t) => (w, t)
            case x => sys.error(f"failed on line $lineNum")
          }.toVector
    }.toVector
  }

  def main(args: Array[String]): Unit = {
    val (arguments, options) = CommandLineUtil.parseArgs(args)

    val tdCutoff = {
      val tdcutoffProvided = options.get("tdcutoff").isDefined
      options.get("tagdict").fold(tdcutoffProvided)(_.toBoolean).option {
        options.get("tdcutoff").fold(0.0)(_.toDouble)
      }
    }

    val trainer = {
      val lambda = options.get("lambda").fold(1.0)(_.toDouble)
      if (options.contains("lambda") && !options.contains("tsmooth") && !options.contains("esmooth")) {
        new AddLambdaSmoothedHmmTrainer[String, String](lambda, "<S>", "<S>", "<E>", "<E>", tdCutoff)
      }
      else if (options.contains("tsmooth") || options.contains("esmooth")) {
        val tsmooth: TransitionDistributioner[String, String] =
          options.getOrElse("tsmooth", "none") match {
            case "addlambda" => new AddLambdaTransitionDistributioner(lambda)
            case "onecount" => new OneCountTransitionDistributioner(lambda, lambda)
            //case "tdtrans" => new TdTransTransitionDistributioner(lambda)
            case "none" | "unsmoothed" | "un" => new UnsmoothedTransitionDistributioner()
          }
        val esmooth: EmissionDistributioner[String, String] =
          options.getOrElse("esmooth", "none") match {
            case "addlambda" => new AddLambdaEmissionDistributioner(lambda)
            case "onecount" => new OneCountEmissionDistributioner(lambda, lambda)
            //case "emnlp" => new EmnlpEmissionDistributioner(lambda)
            case "none" | "unsmoothed" | "un" => new UnsmoothedEmissionDistributioner()
          }
        new SmoothedHmmTrainer(tsmooth, esmooth, "<S>", "<S>", "<E>", "<E>", tdCutoff)
      }
      else {
        new UnsmoothedHmmTrainer("<S>", "<S>", "<E>", "<E>")
      }
    }

    val model = Time.time("training", trainer.train(taggedSentencesFile(options("train"))))

    TaggerEvaluator(model, taggedSentencesFile(options("test")))
  }

}
