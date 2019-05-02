package nlp.a3

object A3Check {

  def main(args: Array[String]): Unit = {

    val data =
      Vector(
        "the dog runs .",
        "the dog walks .",
        "the man walks .",
        "a man walks the dog .",
        "the cat walks .",
        "the dog chases the cat .")
        .map(_.split("\\s+").toVector)

    val test =
      Vector(
        "the dog runs .",
        "the dog walks the man .",
        "a man walks .",
        "the man walks the dog .",
        "the cat walks the dog .",
        "the dog chases the cat .")
        .map(_.split("\\s+").toVector)

    {
      //	10 instances labeled "Yes", 7 of which had value "hello" and 3 "goodbye"
      //	5 instances labeled "No", 2 of which had value "hello", and 3 "goodbye"
      //	A default distribution built from 3 "hello" and 5 "goodbye"

      import nlp.a1.ProbabilityDistribution
      import nlp.a1.ConditionalProbabilityDistribution

      val default = new ProbabilityDistribution(Map("hello" -> 3.0, "goodbye" -> 5.0))
      val cpd = new ConditionalProbabilityDistribution(Map(
        "Yes" -> new ProbabilityDistribution(Map("hello" -> 7.0, "goodbye" -> 3.0)),
        "No" -> new ProbabilityDistribution(Map("hello" -> 2.0, "goodbye" -> 3.0))),
        ???,
        ???,
        default)

      cpd("hello", "Yes") // 0.7, since p(hello | Yes) = 7/10 = 0.7
      cpd("hello", "No") // 0.4, since p(hello | No) = 2/5 = 0.4
      cpd("hello", "unknown") // 0.375, since p_default(hello) = 3/8 = 0.375

      cpd.sample("Yes") // "hello" (70% of the time) or "goodbye" (30%)
      cpd.sample("unknown") // "hello" (60% of the time) or "goodbye" (40%)
    }

    //    if (false) {
    //      val ngt = new UnsmoothedNgramModelTrainer(2)
    //      val ngm = ngt.train(data)
    //
    //      val p1 = ngm.sentenceProb("the dog walks .".split("\\s+").toVector)
    //      println(f"$p1%.6f  ${math.exp(p1)}%.6f") // -2.415914  0.089286
    //
    //      val p2 = ngm.sentenceProb("the cat walks the dog .".split("\\s+").toVector)
    //      println(f"$p2%.6f  ${math.exp(p2)}%.6f") // -5.460436  0.004252
    //
    //      val p3 = ngm.sentenceProb("the cat runs.".split("\\s+").toVector)
    //      println(f"$p3%.6f  ${math.exp(p3)}%.6f") // NaN  NaN
    //    }
    //
    //    if (true) {
    //
    //      import nlpclass.Tokenize
    //      import dhg.util.CollectionUtil._
    //      import dhg.util.FileUtil._
    //      import nlp.a3.UnsmoothedNgramModelTrainer
    //
    //      def fileTokens(filename: String) = {
    //        File(filename).readLines
    //          .split("")
    //          .flatMap(paragraph => Tokenize(paragraph.mkString(" ")))
    //          .map(_.map(_.toLowerCase))
    //          .toVector
    //      }
    //
    //      val trainer = new UnsmoothedNgramModelTrainer(2)
    //
    //      val alice = trainer.train(fileTokens("alice.txt"))
    //      alice.sentenceProb("the last came a little bird , so there was that .".split(" ").toVector)
    //      // -41.39217559191104
    //      alice.sentenceProb("so there was that .".split(" ").toVector)
    //      // -19.451400403614677
    //      alice.sentenceProb("the last came a little bird .".split(" ").toVector)
    //      // -Infinity
    //
    //    }
    //
    //    if (false) {
    //      val ngt = new AddLambdaNgramModelTrainer(3, 1.0)
    //      val ngm = ngt.train(data)
    //
    //      val p3 = ngm.sentenceProb("the cat runs .".split("\\s+").toVector)
    //      println(f"$p3%.6f  ${math.exp(p3)}%.6f") // -7.502922  0.000551
    //    }
    //
    //    if (false) {
    //      val ngt = new AddLambdaNgramModelTrainer(3, 1.0)
    //      val ngm = ngt.train(data)
    //
    //      println(PerplexityNgramModelEvaluator(ngm, test))
    //    }
    //
    //    if (false) {
    //      import nlpclass.Tokenize
    //      import dhg.util.CollectionUtil._
    //      import dhg.util.FileUtil._
    //      import nlp.a3._
    //
    //      def fileTokens(filename: String) = {
    //        File(filename).readLines
    //          .split("")
    //          .flatMap(paragraph => Tokenize(paragraph.mkString(" ")))
    //          .map(_.map(_.toLowerCase))
    //          .toVector
    //      }
    //
    //      val lngt = new AddLambdaNgramModelTrainer(3, 1.0)
    //      val lngm = lngt.train(fileTokens("alice.txt"))
    //
    //      println(PerplexityNgramModelEvaluator(lngm, fileTokens("alice.txt")))
    //      println(PerplexityNgramModelEvaluator(lngm, fileTokens("lookingglass.txt")))
    //      println(PerplexityNgramModelEvaluator(lngm, fileTokens("sherlock.txt")))
    //    }
    //
    //    if (false) {
    //      import nlpclass.Tokenize
    //      import dhg.util.CollectionUtil._
    //      import dhg.util.FileUtil._
    //      import nlp.a3._
    //
    //      def fileTokens(filename: String) = {
    //        File(filename).readLines
    //          .split("")
    //          .flatMap(paragraph => Tokenize(paragraph.mkString(" ")))
    //          .map(_.map(_.toLowerCase))
    //          .toVector
    //      }
    //
    //      val ingt = new InterpolatedNgramModelTrainer(3, 1.0)
    //      val ingm = ingt.train(fileTokens("alice.txt"))
    //
    //      println(PerplexityNgramModelEvaluator(ingm, fileTokens("alice.txt")))
    //      println(PerplexityNgramModelEvaluator(ingm, fileTokens("lookingglass.txt")))
    //      println(PerplexityNgramModelEvaluator(ingm, fileTokens("sherlock.txt")))
    //    }

  }

}
