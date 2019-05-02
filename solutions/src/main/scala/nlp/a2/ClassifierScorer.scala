package nlp.a2

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil._
import nlpclass._

object ClassifierScorer extends ClassifierScorerToImplement {
  def score[Label, Feature, Value](
    naiveBayesModel: Classifier[Label, Feature, Value],
    testInstances: Vector[(Label, Vector[(Feature, Value)])]): Unit = {
    var correct = 0
    val tpm = collection.mutable.Map[Label, Int]()
    val tnm = collection.mutable.Map[Label, Int]()
    val fpm = collection.mutable.Map[Label, Int]()
    val fnm = collection.mutable.Map[Label, Int]()
    var total = 0

    val labelSet = testInstances.map(_._1).toSet
    for ((testLabel, testFeatures) <- testInstances) {
      val predictedLabel = naiveBayesModel.predict(testFeatures)

      if (predictedLabel == testLabel)
        correct += 1

      for (positiveLabel <- labelSet) {
        if (predictedLabel == positiveLabel) {
          if (testLabel == positiveLabel)
            tpm(positiveLabel) = tpm.getOrElse(positiveLabel, 0) + 1
          else
            fpm(positiveLabel) = fpm.getOrElse(positiveLabel, 0) + 1
        }
        else {
          if (testLabel == positiveLabel)
            fnm(positiveLabel) = fnm.getOrElse(positiveLabel, 0) + 1
          else
            tnm(positiveLabel) = tnm.getOrElse(positiveLabel, 0) + 1
        }
      }

      total += 1
    }

    val prf = labelSet.mapTo { l =>
      val tp = tpm.getOrElse(l, 0)
      val tn = tnm.getOrElse(l, 0)
      val fp = fpm.getOrElse(l, 0)
      val fn = fnm.getOrElse(l, 0)

      val p = tp / (tp + fp).toDouble
      val r = tp / (tp + fn).toDouble
      val f = (2.0 * p * r) / (p + r)

      (p, r, f)
    }

    println(f"accuracy = ${correct * 100.0 / total}%.2f  ($correct / $total)")

    val maxLabelLength = labelSet.map(_.toString.length).max
    println(f"${"".padRight(maxLabelLength)}  P      R      F1")
    for ((l, (p, r, f)) <- prf.toVector.sortBy(_._1.toString)) {
      println(f"${l.toString.padRight(maxLabelLength)}  ${p * 100}%.2f  ${r * 100}%.2f  ${f * 100}%.2f")
    }

    val p = prf.map { case (l, (p, r, f)) => p }.avg
    val r = prf.map { case (l, (p, r, f)) => r }.avg
    val f = prf.map { case (l, (p, r, f)) => f }.avg
    println(f"${"avg".padRight(maxLabelLength)}  ${p * 100}%.2f  ${r * 100}%.2f  ${f * 100}%.2f")
  }
}
