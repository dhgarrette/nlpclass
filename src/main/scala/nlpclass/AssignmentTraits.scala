package nlpclass

////////////////////////////////
// Assignment 0
////////////////////////////////

/**
 * For Assignment 0 - Part 5: Counting n-grams
 */
trait NGramCountingToImplement {

  /**
   * Given a vector of words, return a mapping from ngrams
   * to their counts.
   */
  def countNGrams(ngrams: Vector[String]): Map[Vector[String], Int]

}

////////////////////////////////
// Assignment 1
////////////////////////////////

/**
 * For Assignment 1 - Part 5:
 */
trait ProbabilityDistributionToImplement[B] {
  def apply(x: B): Double
  def sample(): B
}

/**
 * For Assignment 1 - Part 5:
 */
trait ConditionalProbabilityDistributionToImplement[A, B] {
  def apply(x: B, given: A): Double
  def sample(given: A): B
}

/**
 * For Assignment 1 - Part 5:
 */
trait FeatureFileAsDistributionsToImplement {
  def fromFile(filename: String): (Set[String], ProbabilityDistributionToImplement[String], Map[String, ConditionalProbabilityDistributionToImplement[String, String]])
}

////////////////////////////////
// Assignment 2
////////////////////////////////

trait Classifier[Label, Feature, Value] {
  def predict(features: Vector[(Feature, Value)]): Label
}

trait ClassifierTrainer[Label, Feature, Value] {
  def train(instances: Vector[(Label, Vector[(Feature, Value)])]): Classifier[Label, Feature, Value]
}

trait ClassifierScorerToImplement {
  def score[Label, Feature, Value](
    classifier: Classifier[Label, Feature, Value],
    testInstances: Vector[(Label, Vector[(Feature, Value)])])
}

/**
 * For Assignment 2 - Part 2:
 */
trait FeatureExtender[Feature, Value] extends (Vector[(Feature, Value)] => Vector[(Feature, Value)]) {

  def extendFeatures(features: Vector[(Feature, Value)]): Vector[(Feature, Value)]
  final override def apply(features: Vector[(Feature, Value)]) = extendFeatures(features)

}

/**
 * For Assignment 2 - Part 2:
 */
class NoOpFeatureExtender[Feature, Value] extends FeatureExtender[Feature, Value] {

  override def extendFeatures(features: Vector[(Feature, Value)]) = features

}

/**
 * For Assignment 2 - Part 2:
 */
class CompositeFeatureExtender[Feature, Value](featureExtenders: Vector[FeatureExtender[Feature, Value]])
  extends FeatureExtender[Feature, Value] {

  override def extendFeatures(features: Vector[(Feature, Value)]) = {
    featureExtenders.foldLeft(features)((z, fe) => fe(z))
  }

}

////////////////////////////////
// Assignment 3
////////////////////////////////

/**
 * For Assignment 3 - Part 2:
 */
trait NgramModelToImplement {

  /**
   * The order of the ngram model.
   *
   * This can be implemented as a `val`.
   */
  def n: Int

  /**
   * Determine the (log) probability of a full sentence.  Return the probability
   * as the logarithm of the probability.
   *
   * USAGE: ngramModel.sentenceProb(Vector("this", "is", "a", "complete", "sentence"))
   */
  def sentenceProb(sentenceTokens: Vector[String]): Double

  /**
   * Generate a sentence based on the model parameters.
   *
   * Return something like: Vector("this", "is", "a", "complete", "sentence")
   */
  def generate(): Vector[String]

}

/**
 * For Assignment 3 - Part 2:
 */
trait NgramModelTrainerToImplement {

  def train(tokenizedSentences: Vector[Vector[String]]): NgramModelToImplement

}

/**
 * For Assignment 3 - Part 2:
 */
trait NgramModelEvaluator extends ((NgramModelToImplement, Vector[Vector[String]]) => Double) {

  override def apply(model: NgramModelToImplement, tokenizedSentences: Vector[Vector[String]]): Double

}

////////////////////////////////
// Assignment 4
////////////////////////////////

trait Tagger[Word, Tag] {

  /**
   * Compute the probability of the tagged sentence.  The result
   * should be represented as a logarithm.
   */
  def sentenceProb(sentence: Vector[(Word, Tag)]): Double

  /**
   * Accepts a sentence of word tokens and returns a sequence of
   * tags corresponding to each of those words.
   */
  def tagSentence(sentence: Vector[Word]): Vector[Tag]

}

trait TaggerTrainer[Word, Tag] {
  def train(taggedSentences: Vector[Vector[(Word, Tag)]]): Tagger[Word, Tag]
}

////////////////////////////////
// Assignment 6
////////////////////////////////

trait Parser {
  def likelihood(t: Tree): Double
  def parse(tokens: Vector[String]): Option[Tree]
  def generate(): Tree
}

trait ParserTrainer {
  def train(trees: Vector[Tree]): Parser
}
