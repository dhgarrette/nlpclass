package nlp.a5

import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import dhg.util.Arm.ManagedCloseable
import dhg.util.Arm.using
import dhg.util.CollectionUtil._
import opennlp.tools.dictionary.Dictionary
import opennlp.tools.postag.POSDictionary
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSSample
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.util.ObjectStreamUtils
import opennlp.tools.util.TrainingParameters
import opennlp.tools.util.model.ModelType
import nlpclass.TaggerTrainer
import nlpclass.Tagger

/**
 * Train a MEMM from gold-labeled data.
 *
 * @param maxIterations
 * @param cutoff			"events" must occur at least this many times to be used during training
 */
class MemmTaggerTrainer(
  maxIterations: Int = 50,
  cutoff: Int = 100)
  extends TaggerTrainer[String, String] {

  private[this]type Word = String
  private[this]type Tag = String

  override def train(taggedSentences: Vector[Vector[(Word, Tag)]]): Tagger[Word, Tag] = {

    val samples = ObjectStreamUtils.createObjectStream(
      taggedSentences.map { s =>
        val (syms, tags) = s.unzip
        new POSSample(syms.toArray, tags.toArray)
      }: _*)

    val languageCode = "Uh... any language ???"

    val params = new TrainingParameters()
    params.put(TrainingParameters.ALGORITHM_PARAM, ModelType.MAXENT.toString)
    params.put(TrainingParameters.ITERATIONS_PARAM, maxIterations.toString)
    params.put(TrainingParameters.CUTOFF_PARAM, cutoff.toString)

    val tagDictionary: POSDictionary = null
    //    new POSDictionary(new BufferedReader(new StringReader({
    //      tagDict.setIterator.mapt((sym, tags) => "%s %s\n".format(sym, tags.mkString(" "))).mkString("\n")
    //    })), true)

    val ngramDictionary: Dictionary = null

    val model = POSTaggerME.train(
      languageCode,
      samples,
      params,
      tagDictionary,
      ngramDictionary)
    new MemmTagger(new POSTaggerME(model))
  }
}

class MemmTagger(meTagger: POSTaggerME) extends Tagger[String, String] {
  private[this]type Word = String
  private[this]type Tag = String

  def sentenceProb(sentence: Vector[(Word, Tag)]): Double = ???

  def tagSentence(sentence: Vector[Word]): Vector[Tag] = {
    meTagger.tag(sentence.toArray).toVector
  }

  def tag(sequences: Vector[Vector[Word]]): Vector[Vector[Tag]] = {
    sequences.map(tagSentence)
  }
}
