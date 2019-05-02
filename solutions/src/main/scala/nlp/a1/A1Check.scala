package nlp.a1

import dhg.util.CollectionUtil._

object A1Check {

  def main(args: Array[String]): Unit = {

    // apply
    
    import nlp.a1.ProbabilityDistribution
    val pd = new ProbabilityDistribution[String](Map("Yes" -> 6, "No" -> 4))
    println(pd("Yes")) // 0.6, since p(Yes) = 0.6

    import nlp.a1.ConditionalProbabilityDistribution
    val cpd = new ConditionalProbabilityDistribution[String, String](Map(
      "Yes" -> new ProbabilityDistribution[String](Map("hello" -> 7, "goodbye" -> 3)),
      "No" -> new ProbabilityDistribution[String](Map("hello" -> 2, "goodbye" -> 3))))
    println(cpd("hello", "Yes")) // 0.7, since p(hello | Yes) = 0.7

    println(pd("unknown")) // 0.0
    println(cpd("unknown", "Yes")) // 0.0
    println(cpd.apply("hello", "unknown")) // 0.0

    // sample
    
    println(Vector.fill(1000)(pd.sample).counts)
    println(Vector.fill(1000)(cpd.sample("Yes")).counts)
    println(Vector.fill(1000)(cpd.sample("No")).counts)

    // features
    
    import nlp.a1.FeatureFileAsDistributions
    val (labels, pLabel, pFeatureValueGivenLabelByFeature) = FeatureFileAsDistributions.fromFile("data2.txt")

    println(labels) // Set(neutral, negative, positive)

    println(f"p(label=negative) = ${pLabel("negative")}%.2f") // 0.57
    println(f"p(label=neutral)  = ${pLabel("neutral")}%.2f") // 0.14
    println(f"p(label=positive) = ${pLabel("positive")}%.2f") // 0.29

    val featureNeg = pFeatureValueGivenLabelByFeature("neg")
    println(f"p(neg=bad | label=negative) = ${featureNeg("bad", "negative")}%.2f") // 0.29

    val featurePos = pFeatureValueGivenLabelByFeature("pos")
    println(f"p(pos=best | label=negative) = ${featurePos("best", "negative")}%.2f") // 1.00
    println(f"p(pos=best | label=positive) = ${featurePos("best", "positive")}%.2f") // 0.25

    val featureWord = pFeatureValueGivenLabelByFeature("word")
    println(f"${featureWord("best", "negative")}%.2f") // p(word=best | label=negative) = 0.07
    println(f"${featureWord("best", "positive")}%.2f") // p(word=best | label=positive) = 0.13

  }

}
