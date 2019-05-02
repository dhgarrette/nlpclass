package nlp.a4

import dhg.util.CollectionUtil._
import dhg.util.Time._
import dhg.util.FileUtil._
import dhg.util.StringUtil._
import math.{ log, exp, abs }
import nlpclass._
import nlp.a1.ConditionalProbabilityDistribution
import com.typesafe.scalalogging.log4j.Logging
import nlp.a1.ProbabilityDistribution
import nlp.a1.ConditionalProbabilityDistribution
import scala.collection.immutable.BitSet
import scala.collection.breakOut
import scala.util.Random
import nlpclass.Utilities._
import annotation.tailrec
import nlp.a5.MemmTaggerTrainer
import breeze.numerics._
import scalaz._
import Scalaz._

object EmHmmInitialization {

  trait TransitionInitializer[Word, Tag] extends (( //
  Vector[Vector[Word]], // sentences
  TagDictionary[Word, Tag] // tagdict
  ) => ConditionalProbabilityDistribution[Tag, Tag]) {
    def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag] //
      ): ConditionalProbabilityDistribution[Tag, Tag]
  }

  class TrUniform[Word, Tag]() extends TransitionInitializer[Word, Tag] {
    override def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag]) = {
      val tagdict = initialTagdict.withWords(sentences.flatten.toSet)
      val allTags = Some(tagdict.allTags)
      val startTag = Some(Set(tagdict.startTag))
      new ConditionalProbabilityDistribution(
        Map(
          tagdict.startTag -> new ProbabilityDistribution(Map(), allTags, Some(Set(tagdict.startTag, tagdict.endTag)), 1.0),
          tagdict.endTag -> new ProbabilityDistribution(Map())),
        None,
        None,
        new ProbabilityDistribution(Map(), Some(tagdict.allTags + tagdict.endTag), startTag, 1.0))
    }
    override def toString = f"TrUniform()"
  }

  abstract class TrTagDictPossibilities[Word, Tag](distributioner: TransitionDistributioner[Word, Tag]) extends TransitionInitializer[Word, Tag] {
    def make(
      sentences: Vector[Vector[Set[Tag]]],
      tagdict: TagDictionary[Word, Tag]) = {
      val potentialTransitions =
        for (
          s <- sentences;
          (as, bs) <- (Set(tagdict.startTag) +: s :+ Set(tagdict.endTag)).sliding2;
          c = 1.0 / (as.size * bs.size);
          a <- as; b <- bs
        ) yield {
          (a, (b, c))
        }
      val trCounts = potentialTransitions.groupByKey.mapVals(_.groupByKey.mapVals(_.sum))
      val tagCounts = sentences.flatten.flatMap(ts => ts.mapToVal(1.0 / ts.size)).groupByKey.mapVals(_.sum)
      distributioner.make(trCounts, tagCounts, tagdict)
    }
  }

  /**
   * TD-TRANS ENTRIES Estimate
   *
   * tdcutoff = 0.1,   lambda = 0.13
   * tdcutoff = 0.01,  lambda = 0.7
   * tdcutoff = 0.001, lambda = 0.6
   * tdcutoff = 0.0,   lambda = 0.5
   *
   * THIS IS BETTER _AND_ FASTER THAN THE "COMPLETE" VERSION!!
   */
  class TrTagDictEntriesPossibilities[Word, Tag](distributioner: TransitionDistributioner[Word, Tag]) extends TrTagDictPossibilities[Word, Tag](distributioner) {
    override def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag]) = {
      val tagdict = initialTagdict.withWords(sentences.flatten.toSet)
      make(sentences.map(_.map(w => tagdict.entries.getOrElse(w, Set()))), tagdict)
    }
    override def toString = f"TrTagDictEntriesPossibilities($distributioner)"
  }

  /**
   * TD-TRANS COMPLETE Estimate
   *
   * tdcutoff = 0.1,   lambda = 0.04
   * tdcutoff = 0.01,  lambda = 0.01
   * tdcutoff = 0.001, lambda = 0.01
   * tdcutoff = 0.0,   lambda = 0.01
   */
  class TrTagDictCompletePossibilities[Word, Tag](distributioner: TransitionDistributioner[Word, Tag]) extends TrTagDictPossibilities[Word, Tag](distributioner) {
    override def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag]) = {
      val tagdict = initialTagdict.withWords(sentences.flatten.toSet)
      make(sentences.map(_.map(tagdict)), tagdict)
    }
    override def toString = f"TrTagDictCompletePossibilities($distributioner)"
  }

  //
  //
  //

  trait EmissionInitializer[Word, Tag] extends (( //
  Vector[Vector[Word]], // sentences
  TagDictionary[Word, Tag] // tagdict
  ) => ConditionalProbabilityDistribution[Tag, Word]) {
    override def toString = this.getClass.getName.toString
    def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag] //
      ): ConditionalProbabilityDistribution[Tag, Word]
  }

  class EmUniform[Word, Tag]() extends EmissionInitializer[Word, Tag] {
    override def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag]) = {
      val tagdict = initialTagdict.withWords(sentences.flatten.toSet)
      val knownWordsForTag = tagdict.entries.ungroup.map(_.swap).groupByKey.mapVals(_.toSet).withDefaultValue(Set.empty)
      val knownWords = knownWordsForTag.flatMap(_._2).toSet
      val allWordsSet = Some(tagdict.allWords)
      new ConditionalProbabilityDistribution(
        tagdict.allTags.mapTo(t => new ProbabilityDistribution(Map(), allWordsSet, Some(knownWords -- knownWordsForTag(t) + tagdict.startWord + tagdict.endWord), 1.0)).toMap +
          (tagdict.startTag -> new ProbabilityDistribution(Map(tagdict.startWord -> 1.0))) +
          (tagdict.endTag -> new ProbabilityDistribution(Map(tagdict.endWord -> 1.0))),
        None, None,
        new ProbabilityDistribution(Map(), allWordsSet, Some(knownWords + tagdict.startWord + tagdict.endWord), 1.0))
    }
    override def toString = f"EmUniform()"
  }

  class EmCheat[Word, Tag](taggedSentences: Vector[Vector[(Word, Tag)]], lambda: Double) extends EmissionInitializer[Word, Tag] {
    override def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag]) = {
      val tagdict = initialTagdict.withWords(sentences.flatten.toSet)
      val knownWordsForTag = tagdict.entries.ungroup.map(_.swap).groupByKey.mapVals(_.toSet).withDefaultValue(Set.empty)
      val knownWords = knownWordsForTag.flatMap(_._2).toSet
      val allWordsSet = Some(tagdict.allWords)
      val normalizedCounts = taggedSentences.flatten.map(_.swap).groupByKey.mapVals(_.counts.mapVals(_.toDouble))
      new ConditionalProbabilityDistribution(
        normalizedCounts.mapt((t, counts) => t -> new ProbabilityDistribution(counts, allWordsSet, Some(knownWords -- knownWordsForTag(t) + tagdict.startWord + tagdict.endWord), lambda)).toMap +
          (tagdict.startTag -> new ProbabilityDistribution(Map(tagdict.startWord -> 1.0))) +
          (tagdict.endTag -> new ProbabilityDistribution(Map(tagdict.endWord -> 1.0))),
        None, None,
        new ProbabilityDistribution(normalizedCounts.values.reduce(_ |+| _), allWordsSet, Some(knownWords + tagdict.startWord + tagdict.endWord), lambda))
    }
    override def toString = f"EmCheat($lambda)"
  }

  /*
   * tdcutoff 0.1,   lambda=0.05, tdCountLambda=0.25  // beats cheating!!
   * tdcutoff 0.01,  lambda=0.05, tdCountLambda=0.25
   * tdcutoff 0.001, lambda=0.04, tdCountLambda=0.25
   * tdcutoff 0.0,   lambda=0.04, tdCountLambda=0.26
   */
  class EmCrazy[Word, Tag](lambda: Double = 0.04, tdCountLambda: Double = 0.26, combineKU: Boolean = false) extends EmissionInitializer[Word, Tag] {
    override def apply(
      sentences: Vector[Vector[Word]],
      initialTagdict: TagDictionary[Word, Tag]) = {

      /* We normally smooth emissions from C(t,w)
       *   p(w|t) = C(t,w) / C(t)
       *   
       * C(t,w) comes in two varieties:
       * 
       *   1. if w is in the TD: C_k(t,w)
       *      - if t in TD(w): C_k(t,w) = C(w) / |TD(w)|
       *        else:          C_k(t,w) = 0
       *   
       *   2. if w is not in the TD: C_u(t,w)      (td-unknown)
       *      - C_u(t,w) = C(w) * p(t|unk)         (divide the counts of w among all tags according 
       *                                            to likelihood of t given that w is unknown)
       *      - p(t|unk) = p(unk|t) * p(t) / Z     (Bayes rule)
       *      - p(t) = sum_w' C_k(t,w') / Z
       *      - p(unk|t) = C(t,unk) / Z XX
       *                 = |TD(t)| / (sum_t' |TD(t')| for known words only)
       *                 
       * p(w|t) = (C_k(t,w) + C_u(t,w)) / Z
       *   
       */

      val sentenceWords = sentences.flatten.toSet
      val tagdict = initialTagdict.withWords(sentences.flatten.toSet)

      val knownWordsForTag = tagdict.knownWordsForTag
      val knownWords = knownWordsForTag.flatMap(_._2).toSet

      val C = sentences.flatten.counts // C(w)

      // C_k(t)(w)
      val C_k = tagdict.entries.mapt { (w, ts) =>
        val partialCounts = (C.getOrElse(w, 0) + tdCountLambda) / ts.size.toDouble
        ts.mapTo(t => w -> partialCounts)
      }.toVector.flatten.groupByKey.mapVals(_.groupByKey.map { case (w, Vector(c)) => w -> c })

      val td_unknown_words = sentenceWords -- knownWords
      // p(t) = sum_w' C_k(t,w') / Z
      val p = tagdict.allTags.mapTo(C_k(_).values.sum).toMap.normalizeValues
      // p(unk|t) = |TD(t)| / (sum_t' |TD(t')| for known words only)
      val `p(unk|t)` = tagdict.allTags.mapTo(knownWordsForTag(_).size).toMap.normalizeValues
      // p(t|unk) = p(unk|t) * p(t) / Z
      val `p(t|unk)` = tagdict.allTags.mapTo(t => `p(unk|t)`(t) * p(t)).toMap.normalizeValues
      // C_u(t)(w) = C(w) * p(t|unk)
      val C_u =
        tagdict.allTags.mapTo { t =>
          td_unknown_words.mapTo { w =>
            C(w) * `p(t|unk)`(t)
          }.toMap
        }.toMap

      val C_ku = tagdict.allTags.mapTo { t =>
        if (combineKU)
          C_u(t) |+| C_k(t)
        else
          C_u(t) ++ C_k(t)
      }.toMap

      //TODO: CHECK THAT EVERY TD-VALID ENTRY HAS A NON-ZERO PROBABILITY
      for (w <- sentenceWords; t <- tagdict(w)) {
        assert(C_ku.contains(t), f"C_ku doesn't contain t=$t")
        assert(C_ku(t).contains(w), f"C_ku(t=$t) doesn't contain w=$w")
        assert(C_ku(t)(w) > 0, f"C_ku(t=$t)(w=$w) = ${C_ku(t)(w)}")
      }

      val allWordsSet = Some(tagdict.allWords)
      new ConditionalProbabilityDistribution(
        C_ku.mapt((t, counts) => t -> new ProbabilityDistribution(counts, allWordsSet, Some(knownWords -- knownWordsForTag(t) + tagdict.startWord + tagdict.endWord), lambda)).toMap +
          (tagdict.startTag -> new ProbabilityDistribution(Map(tagdict.startWord -> 1.0))) +
          (tagdict.endTag -> new ProbabilityDistribution(Map(tagdict.endWord -> 1.0))),
        None, None,
        new ProbabilityDistribution(C_ku.values.reduce(_ |+| _), allWordsSet, Some(knownWords + tagdict.startWord + tagdict.endWord), lambda))
    }
    override def toString = f"EmCrazy($lambda, $tdCountLambda, $combineKU)"
  }

}
