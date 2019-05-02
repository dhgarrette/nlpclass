package nlp.a4

import dhg.util.CollectionUtil._
import dhg.util.Time._
import nlpclass._
import math.{ log, exp }

object A4Check {

  def main(args: Array[String]): Unit = {

    def taggedSentenceString(s: String) = s.split("\\s+").map(_.split("\\|")).map { case Array(w, t) => (w, t) }.toVector

    val smallTrainData = """
		    the|D man|N walks|V the|D dog|N
			the|D dog|N runs|V
			the|D dog|N walks|V
			the|D man|N walks|V
			a|D man|N saw|V the|D dog|N
			the|D cat|N walks|V
      """.split("\n").map(_.trim).filter(_.nonEmpty).map(taggedSentenceString).toVector

    //

    //val trainer = new UnsmoothedHmmTrainer[String, String]("<S>", "<E>", "<S>", "<E>")
//    val trainer = new AddLambdaSmoothedHmmTrainer[String, String](0.1, "<S>", "<E>", "<S>", "<E>")
//    val model = trainer.train(smallTrainData)
//    println(model.tagSentence("the dog runs".split("\\s+").toVector))
//    println(model.tagSentence("the cat runs".split("\\s+").toVector))

    //

//    return

    {
      val trainData = smallTrainData
      val trainer = new UnsmoothedHmmTrainer[String, String]("<S>", "<E>", "<S>", "<E>", None)
      val model = trainer.train(trainData)

      val s1 = Vector(("the", "D"), ("dog", "N"), ("runs", "V"))
      val p1 = model.sentenceProb(s1)
      println(p1)
      println(f"$p1%.4f  ${exp(p1)}%.4f") // -3.3116  0.0365

      val s2 = Vector(("the", "D"), ("cat", "N"), ("runs", "V"))
      val p2 = model.sentenceProb(s2)
      println(p2)
      println(f"$p2%.4f  ${exp(p2)}%.4f") // -4.6979  0.0091

      val s3 = Vector(("the", "D"), ("man", "N"), ("held", "V"), ("the", "D"), ("saw", "N"))
      val p3 = model.sentenceProb(s3)
      println(f"$p3%.4f  ${exp(p3)}%.4f") // -Infinity  0.0000

      println(model.tagSentence("the dog runs".split("\\s+").toVector))
      // Vector(D, N, V)
      println(model.tagSentence("the cat runs".split("\\s+").toVector))
      // Vector(D, N, V)
      println(model.tagSentence("the man held the saw".split("\\s+").toVector))
      // ???
    }
    
    return

    {
      val trainData = Hmm.taggedSentencesFile("ptbtag/train.txt")

      //      val td = trainData.flatten.groupByKey.mapVals(_.toSet)
      //      println(td.get("the")) // Some(Set(DT, NNP, JJ, NN))
      //      println(td.get("walks")) // Some(Set(VBZ))

      val trainer = new UnsmoothedHmmTrainer[String, String]("<S>", "<E>", "<S>", "<E>", None)
      val model = time("ptb unsmoothed train", trainer.train(trainData))
      println(model.sentenceProb(taggedSentenceString("The|DT man|NN saw|VBD a|DT house|NN .|.")))
      // -34.38332797005687
      println(model.tagSentence("The man saw a house .".split("\\s+").toVector))
      // Vector(DT, NN, VBD, DT, NN, .)
    }

    {
      val trainData = smallTrainData
      val trainer = new AddLambdaSmoothedHmmTrainer[String, String](0.1, "<S>", "<E>", "<S>", "<E>", None)
      val model = trainer.train(trainData)

      val s1 = Vector(("the", "D"), ("dog", "N"), ("runs", "V"))
      val p1 = model.sentenceProb(s1)
      println(f"$p1%.4f  ${exp(p1)}%.4f") // -3.6339  0.0264

      val s2 = Vector(("the", "D"), ("cat", "N"), ("runs", "V"))
      val p2 = model.sentenceProb(s2)
      println(f"$p2%.4f  ${exp(p2)}%.4f") // -4.9496  0.0071

      val s3 = Vector(("the", "D"), ("man", "N"), ("held", "V"), ("the", "D"), ("saw", "N"))
      val p3 = model.sentenceProb(s3)
      println(f"$p3%.4f  ${exp(p3)}%.4f") // -13.0951  0.0000

      println(model.tagSentence("the dog runs".split("\\s+").toVector))
      // Vector(D, N, V)
      println(model.tagSentence("the cat runs".split("\\s+").toVector))
      // Vector(D, N, V)
      println(model.tagSentence("the man held the saw".split("\\s+").toVector))
      // Vector(D, N, V, D, N)
    }

    {
      val trainData = Hmm.taggedSentencesFile("ptbtag/train.txt")

      //      val td = trainData.flatten.groupByKey.mapVals(_.toSet)
      //      println(td.get("the")) // Some(Set(DT, NNP, JJ, NN))
      //      println(td.get("walks")) // Some(Set(VBZ))

      val trainer = new AddLambdaSmoothedHmmTrainer[String, String](1.0, "<S>", "<E>", "<S>", "<E>", None)
      val model = time("ptb unsmoothed train", trainer.train(trainData))
      println(model.sentenceProb(taggedSentenceString("The|DT man|NN saw|VBD a|DT house|NN .|.")))
      // -37.56746722307677
      println(model.tagSentence("The man saw a house .".split("\\s+").toVector))
      // Vector(DT, NN, VBD, DT, NN, .)
    }

    //    {
    //      val trainData = Hmm.taggedSentencesFile("tagtest/train.txt")
    //
    //      val unsmoothedtrainer = new UnsmoothedHmmTrainer[String, String](false, None)
    //      val unsmoothedmodel = unsmoothedtrainer.train(trainData)
    //      println(unsmoothedmodel.sentenceProb(taggedSentenceString("the|D dog|N runs|V")))
    //      println(unsmoothedmodel.sentenceProb(taggedSentenceString("the|D cat|N runs|V")))
    //      println(unsmoothedmodel.sentenceProb(taggedSentenceString("the|D man|N held|V the|D saw|N")))
    //
    //      val smoothedtrainer = new AddLambdaSmoothedHmmTrainer[String, String](1.0, false, None)
    //      val smoothedmodel = smoothedtrainer.train(trainData)
    //      println(smoothedmodel.sentenceProb(taggedSentenceString("the|D dog|N runs|V")))
    //      println(smoothedmodel.sentenceProb(taggedSentenceString("the|D cat|N runs|V")))
    //      println(smoothedmodel.sentenceProb(taggedSentenceString("the|D man|N held|V the|D saw|N")))
    //    }

  }

}

