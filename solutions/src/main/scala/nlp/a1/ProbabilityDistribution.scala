package nlp.a1

import scala.util.Random
import dhg.util.CollectionUtil._
import nlpclass.ProbabilityDistributionToImplement
import nlpclass.ConditionalProbabilityDistributionToImplement
import nlpclass.FeatureFileAsDistributionsToImplement
import nlp.a0.CountFeatures
import com.typesafe.scalalogging.log4j.Logging

abstract class AbstractProbabilityDistribution[B]() extends ProbabilityDistributionToImplement[B] {
  def allKnownBs: Set[B]
  def counts(b: B): Double
  def defaultCount: Double

  final def allKnownProbs = allKnownBs.iterator.mapTo(apply)
  final def defaultProb = defaultCount / smoothedCountSum

  final private[this] lazy val smoothedCountSum = allKnownBs.sumBy(counts)

  final def apply(b: B): Double = {
    //    logger.info(f"${x}%-20s  ${counts.getOrElse(x, lambda)}/$countSum")
    //    logger.debug(f"${counts.size}:[${counts.toVector.sortBy(_._1.toString).map { case (w, c) => f"$w -> $c" }.mkString(", ")}]")
    if (smoothedCountSum == 0.0)
      0.0
    else
      counts(b) / smoothedCountSum
  }

  final def sample(): B = {
    assert(allKnownBs.nonEmpty)
    val orderedBs = allKnownBs.toVector.mapTo(counts).sortBy(-_._2)
    var accum = Random.nextDouble * smoothedCountSum
    val it = orderedBs.iterator
    while (true) {
      val (item, count) = it.next
      accum -= count
      if (accum <= 0)
        return item
    }
    sys.error("nothing sampled!")
  }

  final override def toString = f"PD(${allKnownBs.toVector.mapTo(counts).sortBy(-_._2).map(_._1).mapTo(apply).map { case (k, v) => f"$k -> $v" }.mkString(", ")})"
}

class ProbabilityDistribution[B](
  unsmoothedCounts: Map[B, Double],
  knownBs: Option[Set[B]],
  excludedBs: Option[B => Boolean],
  lambda: Double)
  extends AbstractProbabilityDistribution[B]
  with Logging {

  def this(unsmoothedCounts: Map[B, Double]) = this(unsmoothedCounts, None, None, 0.0)

  val allKnownBs = (knownBs.getOrElse(Set()) | unsmoothedCounts.keySet).filterNot(excludedBs.getOrElse(Set()))
  def counts(b: B) = if (excludedBs.isDefined && excludedBs.get(b)) 0.0 else (unsmoothedCounts.getOrElse(b, 0.0) + lambda)
  def defaultCount: Double = lambda
}

class DefaultedProbabilityDistribution[B](
  smoothedCounts: Map[B, Double],
  knownBs: Option[Set[B]],
  excludedBs: Option[B => Boolean],
  val defaultCount: Double)
  extends AbstractProbabilityDistribution[B]
  with Logging {

  def this(smoothedCounts: Map[B, Double]) = this(smoothedCounts, None, None, 0.0)

  val allKnownBs = (knownBs.getOrElse(Set()) | smoothedCounts.keySet).filterNot(excludedBs.getOrElse(Set()))
  def counts(b: B) = if (excludedBs.isDefined && excludedBs.get(b)) 0.0 else (smoothedCounts.getOrElse(b, defaultCount))
}

class ConditionalProbabilityDistribution[A, B](
  unsmoothedConditionedDistributions: Map[A, AbstractProbabilityDistribution[B]],
  knownAs: Option[Set[A]],
  excludedAs: Option[A => Boolean],
  default: AbstractProbabilityDistribution[B])
  extends ConditionalProbabilityDistributionToImplement[A, B] {

  def this(conditionedDistributions: Map[A, AbstractProbabilityDistribution[B]]) = this(conditionedDistributions, None, None, new ProbabilityDistribution[B](Map()))

  val allKnownAs = (knownAs.getOrElse(Set()) | unsmoothedConditionedDistributions.keySet).filterNot(excludedAs.getOrElse(Set()))
  def allKnownConditionedDistributions = allKnownAs.iterator.mapTo(given => unsmoothedConditionedDistributions.getOrElse(given, default))

  def apply(x: B, given: A): Double = {
    if (excludedAs.isDefined && excludedAs.get(given))
      0.0
    else
      unsmoothedConditionedDistributions.getOrElse(given, default)(x)
  }

  def sample(given: A): B = {
    if (excludedAs.isDefined && excludedAs.get(given))
      sys.error(f"cannot sample from $given")
    else
      unsmoothedConditionedDistributions.getOrElse(given, default).sample
  }
}

object FeatureFileAsDistributions extends FeatureFileAsDistributionsToImplement {
  def fromInstances[Feature, Label, Value](instances: Vector[(Feature, Vector[(Label, Value)])]) = {
    val (labels, labelCounts, valueCountsByLabelByFeature) = CountFeatures.featureCounts(instances)
    (labels,
      new ProbabilityDistribution(labelCounts.mapVals(_.toDouble)),
      valueCountsByLabelByFeature.mapVals(conditionedCounts =>
        new ConditionalProbabilityDistribution(
          conditionedCounts.mapVals(counts =>
            new ProbabilityDistribution(counts.mapVals(_.toDouble))))))
  }

  override def fromFile(filename: String): (Set[String], ProbabilityDistribution[String], Map[String, ConditionalProbabilityDistribution[String, String]]) = {
    fromInstances(CountFeatures.readInstancesFromFile(filename))
  }
}
