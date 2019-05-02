package nlp.a4

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import math.{ log, exp }
import nlpclass._
import com.typesafe.scalalogging.log4j.Logging
import nlp.a1._
import scalaz._
import Scalaz._
import dhg.util.Time
import scala.collection.breakOut

trait TransitionDistributioner[Word, Tag] {
  def apply(
    taggedSentences: Vector[Vector[(Word, Tag)]],
    tagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Tag]

  def make(
    transitionCounts: Map[Tag, Map[Tag, Double]],
    tagCounts: Map[Tag, Double], // INCLUDING START/END
    tagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Tag]
}

abstract class AbstractTransitionDistributioner[Word, Tag] extends TransitionDistributioner[Word, Tag] {
  def apply(
    taggedSentences: Vector[Vector[(Word, Tag)]],
    tagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Tag] = {
    val transitionCounts = taggedSentences.flatMap(s => (tagdict.startTag +: s.map(_._2) :+ tagdict.endTag).sliding2).groupByKey.mapVals(_.counts.mapVals(_.toDouble))
    val tagCounts = taggedSentences.flatMap(s => (tagdict.startTag +: s.map(_._2) :+ tagdict.endTag)).counts.mapVals(_.toDouble)
    make(transitionCounts, tagCounts, tagdict)
  }
}

class UnsmoothedTransitionDistributioner[Word, Tag]()
  extends AbstractTransitionDistributioner[Word, Tag] {
  def make(
    transitionCounts: Map[Tag, Map[Tag, Double]],
    tagCounts: Map[Tag, Double], // INCLUDING START/END
    tagdict: TagDictionary[Word, Tag]) = {
    new ConditionalProbabilityDistribution(transitionCounts.mapVals(new ProbabilityDistribution(_)))
  }
  override def toString = f"UnsmoothedTransitionDistributioner()"
}

/**
 * tdcutoff 0.1,   lambda=0.23
 * tdcutoff 0.01,  lambda=0.21
 * tdcutoff 0.001, lambda=0.20
 * tdcutoff 0.0,   lambda=0.20
 */
class AddLambdaTransitionDistributioner[Word, Tag](lambda: Double = 0.2)
  extends AbstractTransitionDistributioner[Word, Tag] {
  def make(
    transitionCounts: Map[Tag, Map[Tag, Double]],
    tagCounts: Map[Tag, Double], // INCLUDING START/END
    tagdict: TagDictionary[Word, Tag]) = {
    val allTransitionToAbleTags = Some(tagdict.allTags + tagdict.endTag)
    val startTag = Some(Set(tagdict.startTag))
    new ConditionalProbabilityDistribution(
      transitionCounts.mapVals(new ProbabilityDistribution(_, allTransitionToAbleTags, startTag, lambda)) +
        (tagdict.startTag -> new ProbabilityDistribution(transitionCounts(tagdict.startTag), allTransitionToAbleTags, Some(Set(tagdict.startTag, tagdict.endTag)), lambda)) + // Start Tag can't transition to End Tag
        (tagdict.endTag -> new ProbabilityDistribution(Map())), // End Tag can't transition to anything
      None, None, new ProbabilityDistribution(Map(), allTransitionToAbleTags, startTag, lambda))
  }
  override def toString = f"AddLambdaTransitionDistributioner($lambda)"
}

/**
 *                    C(t_{i-1}, t_i) + a(t_{i-1}) * p(t_i)
 * p(t_i | t_{i-1}) = -------------------------------------
 *                          C(t_{i-1}) + a(t_{i-1})
 *     where a(t_{i-1}) = |t_i : C(t_{i-1}, t_i) = 1| + \epsilon
 */
class OneCountTransitionDistributioner[Word, Tag](singletonEpsilon: Double, tagPriorLambda: Double, flatPrior: Boolean = false, flatSingletons: Boolean = false)
  extends AbstractTransitionDistributioner[Word, Tag] {
  def make(
    transitionCounts: Map[Tag, Map[Tag, Double]],
    tagCounts: Map[Tag, Double], // INCLUDING START/END
    tagdict: TagDictionary[Word, Tag]) = {
    val allTransitionToAbleTags = tagdict.allTags + tagdict.endTag
    val someAllTransitionToAbleTags = Some(allTransitionToAbleTags)
    val startTag = Some(Set(tagdict.startTag))
    val seTags = Some(Set(tagdict.startTag, tagdict.endTag))

    val singletonTransitions =
      if (flatSingletons)
        Map[Tag, Double]().withDefaultValue(tagPriorLambda * someAllTransitionToAbleTags.get.size)
      else
        (tagdict.allTags + tagdict.startTag).mapTo { t => transitionCounts.get(t).fold(0)(_.count(_._2 < 1.00000001)) + singletonEpsilon }.toMap

    val transitionToAbleTagPrior =
      if (flatPrior) {
        if (flatSingletons)
          new DefaultedProbabilityDistribution(Map(), someAllTransitionToAbleTags, startTag, 1.0 / someAllTransitionToAbleTags.get.size)
        else
          new DefaultedProbabilityDistribution(Map(), someAllTransitionToAbleTags, startTag, tagPriorLambda)
      }
      else
        new ProbabilityDistribution(tagCounts, someAllTransitionToAbleTags, startTag, tagPriorLambda)
    val smoothedTransitionCounts =
      (tagdict.allTags + tagdict.startTag).mapTo { tPrime =>
        allTransitionToAbleTags.mapTo { t =>
          val c = (for (tPrimeCounts <- transitionCounts.get(tPrime); c <- tPrimeCounts.get(t)) yield c).getOrElse(0.0)
          c + singletonTransitions(tPrime) * transitionToAbleTagPrior(t)
        }.toMap
      }.toMap
    new ConditionalProbabilityDistribution[Tag, Tag](
      smoothedTransitionCounts.mapt { (tPrime, counts) =>
        tPrime -> new DefaultedProbabilityDistribution(counts, someAllTransitionToAbleTags, startTag, singletonTransitions(tPrime) * transitionToAbleTagPrior.defaultProb)
      } +
        (tagdict.startTag -> {
          val startDestinationTagPrior =
            if (flatPrior) {
              if (flatSingletons)
                new DefaultedProbabilityDistribution(Map(), someAllTransitionToAbleTags, startTag, 1.0 / someAllTransitionToAbleTags.get.size)
              else
                new DefaultedProbabilityDistribution(Map(), someAllTransitionToAbleTags, startTag, tagPriorLambda)
            }
            else
              new ProbabilityDistribution(tagCounts, someAllTransitionToAbleTags, seTags, tagPriorLambda)
          val startSmoothedCounts = tagdict.allTags.mapTo { t => transitionCounts(tagdict.startTag).getOrElse(t, 0.0) + singletonTransitions(tagdict.startTag) * startDestinationTagPrior(t) }.toMap
          val default = singletonTransitions(tagdict.startTag) * startDestinationTagPrior.defaultProb
          new DefaultedProbabilityDistribution(startSmoothedCounts, someAllTransitionToAbleTags, seTags, default) // Start Tag can't transition to End Tag
        }) +
        (tagdict.endTag -> new ProbabilityDistribution(Map())), // End Tag can't transition to anything
      None, None, transitionToAbleTagPrior)
  }
  override def toString = f"OneCountTransitionDistributioner($singletonEpsilon, $tagPriorLambda)"
}

//
//
//

trait EmissionDistributioner[Word, Tag] {
  def apply(
    taggedSentences: Vector[Vector[(Word, Tag)]],
    tagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Word]
}

class UnsmoothedEmissionDistributioner[Word, Tag]()
  extends EmissionDistributioner[Word, Tag] {
  def apply(
    taggedSentences: Vector[Vector[(Word, Tag)]],
    initialTagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Word] = {

    val tagdict = initialTagdict.withWords(taggedSentences.flatten.map(_._1).to[Set]).withTags(taggedSentences.flatten.map(_._2).to[Set])
    val emissionCounts = taggedSentences.flatten.filter { case (w, t) => tagdict(w)(t) }.map(_.swap).groupByKey.mapVals(_.counts.mapVals(_.toDouble))
    val knownWordsForTag = tagdict.entries.ungroup.map(_.swap).groupByKey.mapVals(_.toSet).withDefaultValue(Set.empty)
    val knownWords = knownWordsForTag.flatMap(_._2).toSet
    val allWordsSet = Some(tagdict.allWords)
    new ConditionalProbabilityDistribution(
      emissionCounts.mapt((t, tcounts) => t -> new ProbabilityDistribution(tcounts, allWordsSet, Some(knownWords -- knownWordsForTag(t) + tagdict.startWord + tagdict.endWord), 0.0)) +
        (tagdict.startTag -> new ProbabilityDistribution(Map(tagdict.startWord -> 1.0))) +
        (tagdict.endTag -> new ProbabilityDistribution(Map(tagdict.endWord -> 1.0))))
  }
  override def toString = f"UnsmoothedEmissionDistributioner()"
}

/*
 * tdcutoff 0.1,   lambda=0.10
 * tdcutoff 0.01,  lambda=0.10
 * tdcutoff 0.001, lambda=0.10
 * tdcutoff 0.0,   lambda=0.10
 */
class AddLambdaEmissionDistributioner[Word, Tag](lambda: Double = 0.1)
  extends EmissionDistributioner[Word, Tag] {
  def apply(
    taggedSentences: Vector[Vector[(Word, Tag)]],
    initialTagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Word] = {

    val tagdict = initialTagdict.withWords(taggedSentences.flatten.map(_._1).to[Set]).withTags(taggedSentences.flatten.map(_._2).to[Set])
    val emissionCounts = taggedSentences.flatten.filter { case (w, t) => tagdict(w)(t) }.map(_.swap).groupByKey.mapVals(_.counts.mapVals(_.toDouble))
    val knownWordsForTag = tagdict.entries.ungroup.map(_.swap).groupByKey.mapVals(_.toSet).withDefaultValue(Set.empty)
    val knownWords = knownWordsForTag.flatMap(_._2).toSet
    val allWordsSet = Some(tagdict.allWords)
    new ConditionalProbabilityDistribution(
      emissionCounts.mapt((t, tcounts) => t -> new ProbabilityDistribution(tcounts, allWordsSet, Some(knownWords -- knownWordsForTag(t) + tagdict.startWord + tagdict.endWord), lambda)) +
        (tagdict.startTag -> new ProbabilityDistribution(Map(tagdict.startWord -> 1.0))) +
        (tagdict.endTag -> new ProbabilityDistribution(Map(tagdict.endWord -> 1.0))),
      None, None,
      new ProbabilityDistribution(Map(), allWordsSet, Some(knownWords + tagdict.startWord + tagdict.endWord), 1.0))
  }
  override def toString = f"AddLambdaEmissionDistributioner($lambda)"
}

/**
 *            C(t,w) + b(t) * p(w)
 * p(w | t) = --------------------
 *                C(t) + b(t)
 *     where b(t) = |w : C(t,w) = 1| + \epsilon
 */
class OneCountEmissionDistributioner[Word, Tag](singletonEpsilon: Double, wordPriorLambda: Double)
  extends EmissionDistributioner[Word, Tag] {
  def apply(
    taggedSentences: Vector[Vector[(Word, Tag)]],
    initialTagdict: TagDictionary[Word, Tag]): ConditionalProbabilityDistribution[Tag, Word] = {

    val tagdict = initialTagdict.withWords(taggedSentences.flatten.map(_._1).to[Set]).withTags(taggedSentences.flatten.map(_._2).to[Set])
    val emissionCounts = taggedSentences.flatten.filter { case (w, t) => tagdict(w)(t) }.map(_.swap).groupByKey.mapVals(_.counts.mapVals(_.toDouble))
    val knownWordsForTag = tagdict.entries.ungroup.map(_.swap).groupByKey.mapVals(_.toSet).withDefaultValue(Set.empty)
    val knownWords = knownWordsForTag.flatMap(_._2).toSet
    val allWordsSet = Some(tagdict.allWords)

    val singletonEmissions = tagdict.allTags.mapTo { t => emissionCounts.get(t).fold(0)(_.count(_._2 < 1.00000001)) + singletonEpsilon }.toMap

    val wordCounts = emissionCounts.values.reduce(_ |+| _)
    val wordPrior = new ProbabilityDistribution(wordCounts, Some(tagdict.allWords), Some(Set(tagdict.startWord, tagdict.endWord)), wordPriorLambda)

    val smoothedEmissionCounts =
      tagdict.allTags.mapTo { t =>
        tagdict.allWords.mapTo { w =>
          val c = (for (tCounts <- emissionCounts.get(t); c <- tCounts.get(w)) yield c).getOrElse(0.0)
          c + singletonEmissions(t) * wordPrior(w)
        }.toMap
      }.toMap
    new ConditionalProbabilityDistribution(
      smoothedEmissionCounts.mapt { (t, tcounts) =>
        val default = singletonEmissions(t) * wordPrior.defaultProb
        t -> new DefaultedProbabilityDistribution(tcounts, allWordsSet, Some(knownWords -- knownWordsForTag(t) + tagdict.startWord + tagdict.endWord), default)
      } +
        (tagdict.startTag -> new ProbabilityDistribution(Map(tagdict.startWord -> 1.0))) +
        (tagdict.endTag -> new ProbabilityDistribution(Map(tagdict.endWord -> 1.0))),
      None, None,
      wordPrior)
  }
  override def toString = f"OneCountEmissionDistributioner($singletonEpsilon, $wordPriorLambda)"
}
