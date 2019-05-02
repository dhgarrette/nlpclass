package nlpclass

import java.io.FileReader

import com.typesafe.scalalogging.log4j.Logging

import dhg.util.FileUtil._
import opennlp.maxent.BasicEventStream
import opennlp.maxent.GIS
import opennlp.maxent.ModelApplier
import opennlp.maxent.PlainTextByLineDataStream
import opennlp.maxent.io.SuffixSensitiveGISModelWriter
import opennlp.model.AbstractModel
import opennlp.model.Event
import opennlp.model.EventStream
import opennlp.model.GenericModelReader

class EventStreamIterator(eventStream: EventStream) extends Iterator[Event] {
  override def hasNext() = eventStream.hasNext()
  override def next() = eventStream.next()
}

class IteratorEventStream(itr: Iterator[Event]) extends EventStream {
  override def hasNext() = itr.hasNext
  override def next() = itr.next()
}

class MaxEntModel(
  model: AbstractModel,
  featureExtender: FeatureExtender[String, String] = new NoOpFeatureExtender[String, String])
  extends Classifier[String, String, String] with Logging {
  private[this] val predictor = new ModelApplier(model)

  private[this]type Feature = String
  private[this]type Label = String
  private[this]type Value = String

  def predict(features: Vector[(Feature, Value)]): Label = {
    val context = MaxEntClassifier.featureValueListAsContext(featureExtender(features)).toArray
    val ocs = model.eval(context).toVector
    val result = ocs.zipWithIndex.map { case (ocsi, i) => (model.getOutcome(i), ocsi) }
    val sorted = result.sortBy { case (s, d) => (-d, s) }
    // Print the most likely outcome first, down to the least likely.
    logger.info(sorted.map { case (s, d) => f"$s $d" }.mkString(" "))
    sorted.head._1
  }

  def saveToFile(modelFilename: String) {
    new SuffixSensitiveGISModelWriter(model, new File(modelFilename)).persist()
  }

  def withFeatureExtender(newFeatureExtender: FeatureExtender[String, String]) = {
    new MaxEntModel(this.model, newFeatureExtender)
  }

}

object MaxEntModel {

  def fromFile(modelFilename: String) = {
    new MaxEntModel(new GenericModelReader(new File(modelFilename)).getModel())
  }

}

class MaxEntModelTrainer(
  featureExtender: FeatureExtender[String, String] = new NoOpFeatureExtender[String, String],
  sigma: Double = 1.0, maxIterations: Int = 100, cutoff: Int = 1)
  extends ClassifierTrainer[String, String, String] {
  val USE_SMOOTHING = false
  val SMOOTHING_OBSERVATION = 0.1
  GIS.SMOOTHING_OBSERVATION = SMOOTHING_OBSERVATION

  private[this]type Feature = String
  private[this]type Label = String
  private[this]type Value = String

  def train(instances: Vector[(Label, Vector[(Feature, Value)])]): MaxEntModel = {
    val eventStream = new IteratorEventStream(
      instances.iterator.map {
        case (label, featureValues) =>
          val context = MaxEntClassifier.featureValueListAsContext(featureExtender(featureValues))
          new Event(label, context.toArray)
      })
    new MaxEntModel(GIS.trainModel(eventStream, maxIterations, cutoff, sigma), featureExtender)
  }
}

object MaxEntClassifier {

  def featureValueListAsContext(featureValues: Vector[(String, String)]) = {
    featureValues.map { case (f, v) => s"$f=$v" }
  }

  implicit class EventStreamWithToIterator(val eventStream: EventStream) extends AnyVal {
    def iterator: Iterator[Event] = new EventStreamIterator(eventStream)
  }

}
