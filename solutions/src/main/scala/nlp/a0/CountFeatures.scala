package nlp.a0

import dhg.util.FileUtil._
import dhg.util.CollectionUtil._
import scalaz._
import scalaz.Scalaz._

object CountFeatures {

  /**
   * Count using nested mutable maps -- procedural style
   */
  def getCountsMutable[Feature, Label, Value](instances: Vector[(Label, Vector[(Feature, Value)])]): Map[Feature, Map[Label, Map[Value, Int]]] = {
    /*
     * Make a big mutable map mapping
     *   Features -> Labels -> Values -> Counts
     */
    val featureMap = collection.mutable.Map[Feature, collection.mutable.Map[Label, collection.mutable.Map[Value, Int]]]()

    /*
     * Loop through the instances, adding 1 to the mutable map for each 
     * (feature, label, value) triple
     */
    for (
      (label, fvPairs) <- instances;
      (feature, value) <- fvPairs
    ) {
      val labelMap = featureMap.getOrElseUpdate(feature, collection.mutable.Map.empty)
      val valueMap = labelMap.getOrElseUpdate(label, collection.mutable.Map.empty)
      val existingCount = valueMap.getOrElse(value, 0)
      valueMap(value) = existingCount + 1
    }

    /*
     * Convert mutable maps back into immutable for returning
     */
    featureMap.toMap
      .map {
        case (feature, labelMap) =>
          feature -> labelMap.toMap
            .map {
              case (label, valueMap) =>
                label -> valueMap.toMap
            }
      }
  }

  /**
   * Count using only built-in Scala functional features
   */
  def getCountsFunctional[Feature, Label, Value](instances: Vector[(Label, Vector[(Feature, Value)])]): Map[Feature, Map[Label, Map[Value, Int]]] = {
    /*
     * Flatten the instances into (feature, label, value) triples
     */
    val flvTriples =
      for (
        (label, fvPairs) <- instances;
        (feature, value) <- fvPairs
      ) yield {
        (feature, label, value)
      }

    flvTriples.groupBy(_._1).map { // group by features
      case (feature, groupedByFeature) => // feature mapped to all triples with that feature
        feature ->
          groupedByFeature.groupBy(_._2).map { // group by label
            case (label, groupedByLabel) => // label mapped to all triples with that feature and label combo
              label ->
                groupedByLabel.groupBy(_._3).map { // group by value
                  case (value, groupedByValue) => // value mapped to all triples with that feature, label, and value combo
                    value -> groupedByValue.size // count the number of triples with that feature, label, and value combo
                }
          }
    }
  }

  /**
   * Count using my functional features
   */
  def getCountsFunctionalBetter[Feature, Label, Value](instances: Vector[(Label, Vector[(Feature, Value)])]): Map[Feature, Map[Label, Map[Value, Int]]] = {
    /*
     * Flatten the instances into (feature, label, value) triples
     */
    val flvTriples = instances.ungroup.map { case (label, (feature, value)) => (feature, (label, value)) }

    flvTriples.groupByKey.mapVals { labelValuePairs => // group by features
      labelValuePairs.groupByKey.mapVals { values => // group by label
        values.counts // count by values 
      }
    }
  }

  def readInstancesFromFile(filename: String) = {
    File(filename).readLines.map { line =>
      val featurePairs :+ label = line.split(",").toVector // split into feature pairs and a final label
      label.trim -> featurePairs.map(_.split("=",2).map(_.trim).toTuple2) // turn feature pairs into tuples
    }.toVector
  }

  def featureCounts[Feature, Label, Value](instances: Vector[(Label, Vector[(Feature, Value)])]) = {
    val labelCounts = instances.map { case (label, _) => label }.counts // count just the labels
    val valueCountsByLabelByFeature = getCountsFunctional(instances)
    (labelCounts.keySet, labelCounts, valueCountsByLabelByFeature)
  }

  def main(args: Array[String]): Unit = {
    val Seq(filename) = args.toList

    /*
     * Count all the things
     */
    val (labels, labelCounts, valueCountsByLabelByFeature) = featureCounts(readInstancesFromFile(filename))

    /*
     * Print out label counts
     */
    for ((label, count) <- labelCounts.toVector.sortBy(_._1))
      println(f"$label%-7s $count")
    println

    /*
     * Print out feature value counts
     */
    for ((feature, valueCountsByLabel) <- valueCountsByLabelByFeature.toVector.sortBy(_._1)) {
      println(f"$feature")
      for ((label, valueCounts) <- valueCountsByLabel.toVector.sortBy(_._1)) {
        println(f"    $label")
        for ((value, count) <- valueCounts.toVector.sortBy(_._1)) {
          println(f"        $value%-15s $count")
        }
      }
    }
  }

}
