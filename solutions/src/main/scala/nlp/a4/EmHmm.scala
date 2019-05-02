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
import EmHmmInitialization._

final class EmHmmTrainer[Word, Tag](
  trInitializer: TransitionInitializer[Word, Tag], emInitializer: EmissionInitializer[Word, Tag],
  maxIterations: Int, convergence: Double = 1e-10) {

  final def train(
    sentences: Vector[Vector[Word]], initialTagdict: TagDictionary[Word, Tag]) = {
    val tagdict = initialTagdict.withWords(sentences.flatten.toSet)

    val allWords = tagdict.startWord +: tagdict.endWord +: sentences.flatten.toSet.toVector
    val allTags = tagdict.startTag +: tagdict.endTag +: tagdict.allTags.toVector

    val numWords = allWords.size
    val numTags = allTags.size

    println(f"raw tokens = ${sentences.flatten.size}  (${sentences.size} sentences)")
    println("numWords = " + numWords)
    println("numTags  = " + numTags)
    println

    val wordIndex = allWords.zipWithIndex.toMap
    val tagIndex = allTags.zipWithIndex.toMap

    val td: Array[Array[Int]] = Array(0) +: Array(1) +: {
      val fullTagsetSet = (2 until numTags).toArray
      allWords.drop(2).map { w =>
        val tdws = tagdict(w)
        if (tdws.size < numTags - 2) // incomplete set of tags
          tdws.map(tagIndex)(breakOut): Array[Int]
        else // complete set of tags
          fullTagsetSet // re-use the same array
      }(breakOut): Array[Array[Int]]
    }

    val rtda: Array[Array[Int]] = Array(0) +: Array(1) +: {
      allTags.drop(2).map { t =>
        allWords.zipWithIndex.drop(2).collect { case (w, wi) if tagdict(w)(t) => wi }(breakOut): Array[Int]
      }(breakOut): Array[Array[Int]]
    }

    //    for ((ttd, t) <- rtd.zipWithIndex) {
    //      println(allTags(t) + f" -> (${ttd.size}) " + ttd.take(25).map(allWords).mkString(" "))
    //    }

    val sents: Vector[Array[Int]] = sentences.map(s => (tagdict.startWord +: s :+ tagdict.endWord).map(wordIndex).toArray)

    println("Make Transition Distributions")
    val transitions = trInitializer(sentences, tagdict)
    println("Make Emission Distributions")
    val emissions = emInitializer(sentences, tagdict)

    println("Make Indexed Distributions")
    val tr: Array[Array[Double]] = Array.tabulate(numTags) { t1 => Array.tabulate(numTags) { t2 => transitions(allTags(t2), allTags(t1)) } }
    val em: Array[Array[Double]] = Array.tabulate(numTags) { t => Array.tabulate(numWords) { w => emissions(allWords(w), allTags(t)) } }

    //    println("\nTRANSITIONS\n")
    //    for (t1 <- 0 until numTags) {
    //      for (t2 <- (0 until numTags).toVector.sortBy(t2 => -transitions(t2, t1))) {
    //        println(f"${allTags(t1)}%-5s -> ${allTags(t2)}%-10s  ${transitions(t2, t1)}%.2f  ${log(transitions(t2, t1))}%.2f")
    //      }
    //      println
    //    }

    //    println("\nEMISSIONS\n")
    //    for (t <- 0 until numTags) {
    //      for (w <- (0 until numWords).toVector.sortBy(w => -emissions(w, t)).take(20)) {
    //        println(f"${allTags(t)}%-5s -> ${allWords(w)}%-20s  ${emissions(w, t)}%.2f  ${log(emissions(w, t))}%.2f")
    //      }
    //      println
    //    }

    //    def choose(s: BitSet) = { Random.shuffle(s.toVector).head }
    //    val tagSequences = sents.map(_.map { w => choose(td(w)) })

    //    // INITIAL SENTENCE TAGGING
    //    for (s <- (tagSequences zipSafe sents)) {
    //      println(s.zipped.map { case (t, w) => f"${allWords(w)}|${allTags(t)}" }.mkString(" "))
    //    }

    println("Start Training")
    val (newTr, newEm) = doTrain(sents, numWords, numTags, td, rtda, tr, em)

    val emLearnedTr = {
      val trProbs =
        (0 until numTags).map(k1 => allTags(k1) ->
          (0 until numTags).map(k2 => allTags(k2) ->
            (if (k1 < 2 && k2 < 2) 0.0 else newTr(k1)(k2))).toMap).toMap +
          (tagdict.endTag -> Map[Tag, Double]())
      new ConditionalProbabilityDistribution(trProbs.mapVals(new ProbabilityDistribution(_)))
    }

    val emLearnedEm = {
      val emProbs =
        (2 until numTags).map(t => allTags(t) ->
          rtda(t).map(w => allWords(w) ->
            newEm(t)(w)).toMap).toMap +
          (tagdict.startTag -> Map(tagdict.startWord -> 1.0)) + (tagdict.endTag -> Map(tagdict.endWord -> 1.0))
      new ConditionalProbabilityDistribution(emProbs.mapVals(new ProbabilityDistribution(_)))
    }

    new HiddenMarkovModel(
      emLearnedTr, emLearnedEm,
      tagdict)
  }

  /**
   * @return: learned Transition and Emission probability distributions
   */
  final def doTrain(
    sents: Vector[Array[Int]],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    tr: Array[Array[Double]], em: Array[Array[Double]]) = {
    val logTr = tr.map(_.map(log))
    val logEm = em.map(_.map(log))
    val (newLogTr, newLogEm) = iterate(sents, numWords, numTags, td, rtda, logTr, logEm, 0, Double.NegativeInfinity)
    (newLogTr.map(_.map(exp)), newLogEm.map(_.map(exp)))
  }

  /**
   * @return: learned Transition and Emission probability distributions
   */
  @tailrec private[this] def iterate(
    sents: Vector[Array[Int]],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]],
    iteration: Int, prevAvgLogProb: Double //
    ): (Array[Array[Double]], Array[Array[Double]]) = {

    if (iteration < maxIterations) {
      val startTime = System.currentTimeMillis()
      val (newLogTr, newLogEm, avgLogProb) = reestimate(sents, numWords, numTags, td, rtda, logTr, logEm)
      println(f"iteration ${((iteration + 1) + ":").padRight(4)} ${(System.currentTimeMillis() - startTime) / 1000.0}%.3f sec   avgLogProb=${(avgLogProb + ",").padRight(22)} avgProb=${exp(avgLogProb)}")
      if (avgLogProb - prevAvgLogProb < convergence) {
        println(f"CONVERGENCE (< $convergence)")
        (newLogTr, newLogEm)
      }
      else if (avgLogProb < prevAvgLogProb) {
        println(f"DIVERGENCE!")
        (newLogTr, newLogEm)
      }
      else {
        iterate(sents, numWords, numTags, td, rtda, newLogTr, newLogEm, iteration + 1, avgLogProb)
      }
    }
    else {
      println(f"MAX ITERATIONS REACHED")
      (logTr, logEm)
    }
  }

  private[this] def reestimate(
    sents: Vector[Array[Int]],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]] //
    ) = {

    val expectedTrCounts: Array[Array[Double]] = {
      val data = new Array[Array[Double]](numTags)
      var i = 0; while (i < numTags) { data(i) = new Array[Double](numTags); i += 1 }
      data
    }
    val expectedEmCounts: Array[Array[Double]] = {
      val data = new Array[Array[Double]](numTags)
      var i = 0; while (i < numTags) { data(i) = new Array[Double](numWords); i += 1 }
      data
    }

    var logProbSum = 0.0
    for (s <- sents.seq) {
      logProbSum += contributeExpectations(expectedTrCounts, expectedEmCounts, s, numWords, numTags, td, rtda, logTr, logEm)
    }

    // newLogTr
    //   1. Divide by sum (to get probability)
    //   2. Log
    var k1 = 0
    while (k1 < numTags) {
      val expectedTrCountsk1 = expectedTrCounts(k1)
      var expectedTrCountsk1Sum = 0.0
      var k2 = 0
      while (k2 < numTags) {
        expectedTrCountsk1Sum += expectedTrCountsk1(k2)
        k2 += 1
      }
      k2 = 0
      while (k2 < numTags) {
        expectedTrCountsk1(k2) = log(expectedTrCountsk1(k2) / expectedTrCountsk1Sum)
        k2 += 1
      }
      k1 += 1
    }

    // newLogEm
    //   1. Divide by sum (to get probability) 
    //   2. Log
    var k = 0
    while (k < numTags) {
      val expectedEmCountsk = expectedEmCounts(k)
      var expectedEmCountskSum = 0.0
      val rtdaK = rtda(k)
      val rtdaKLen = rtdaK.length
      var i = 0
      while (i < rtdaKLen) {
        val w = rtdaK(i)
        expectedEmCountskSum += expectedEmCountsk(w)
        i += 1
      }
      i = 0
      while (i < rtdaKLen) {
        val w = rtdaK(i)
        expectedEmCountsk(w) = log(expectedEmCountsk(w) / expectedEmCountskSum)
        i += 1
      }
      k += 1
    }

    //    println((0 until numTags).flatMap(k1 => (0 until numTags).map(k2 => f"exTr($k1)($k2)=${exTr(k1)(k2)}")).mkString(" "))
    //    println((0 until numTags).flatMap(k => (0 until numWords).map(w => f"exEm($k)($w)=${exEm(k)(w)}")).mkString(" "))

    // At this point the "count" are actually probabilities!!
    (expectedTrCounts, expectedEmCounts, logProbSum / sents.size)
  }

  /*
   * Forward-Backward
   */
  private[this] def contributeExpectations(
    expectedTrCounts: Array[Array[Double]],
    expectedEmCounts: Array[Array[Double]],
    w: Array[Int],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]]) = {

    //assert(w.head == 0 && w.last == 0)

    val logFwd = calculateForward(w, numWords, numTags, td, rtda, logTr, logEm)
    val logBkd = calculateBackwrd(w, numWords, numTags, td, rtda, logTr, logEm)

    val logFwdP = logFwd.last(1)
    val logBkdP = logBkd.head(0)
    assert(abs(logFwdP - logBkdP) < 1e-10, f"$logFwdP != $logBkdP")

    contributeExpectedTrCounts(expectedTrCounts, w, numWords, numTags, td, rtda, logTr, logEm, logFwd, logFwdP, logBkd, logBkdP)
    contributeExpectedEmCounts(expectedEmCounts, w, numWords, numTags, td, rtda, logTr, logEm, logFwd, logFwdP, logBkd, logBkdP)

    logFwdP
  }

  private[this] def calculateForward(
    w: Array[Int],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]]) = {
    //println("FORWARD")
    val logFwd = {
      val wLen = w.length
      val data = new Array[Array[Double]](wLen)
      var i = 0; while (i < wLen) { data(i) = new Array[Double](numTags); i += 1 }
      data
    }

    //logFwd(0)(0) = 0.0
    var i = 1
    while (i < w.length) {
      val curLogFwd = logFwd(i)
      val prevLogFwd = logFwd(i - 1)

      val curW = w(i)
      val curWKs = td(curW)
      val curWKsLen = curWKs.length
      val prevKs = td(w(i - 1))
      val prevKsLen = prevKs.length

      var j = 0
      while (j < curWKsLen) {
        val k = curWKs(j)
        val logValue = new Array[Double](prevKsLen)
        var l = 0
        while (l < prevKsLen) {
          val k1 = prevKs(l)
          logValue(l) = logTr(k1)(k) + prevLogFwd(k1)
          l += 1
        }
        curLogFwd(k) = logSum(logValue, prevKsLen) + logEm(k)(curW)
        j += 1
      }
      //println(f"$i%3d: " + curLogFwd.zipWithIndex.map { case (v, k) => if (td(w(i)).contains(k)) exp(v).toString else "" }.map(_.padRight(30)).mkString(" "))
      i += 1
    }
    logFwd
  }

  private[this] def calculateBackwrd(
    w: Array[Int],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]]) = {
    //println("BACKWARD")
    val logBkd = {
      val wLen = w.length
      val data = new Array[Array[Double]](wLen)
      var i = 0; while (i < wLen) { data(i) = new Array[Double](numTags); i += 1 }
      data
    }

    //logBkd(w.length-1)(0) = 0.0
    var i = w.length - 2
    while (i >= 0) {
      val curLogBkd = logBkd(i)
      val nextLogBkd = logBkd(i + 1)

      val curW = w(i)
      val curWKs = td(curW)
      val curWKsLen = curWKs.length
      val nextW = w(i + 1)
      val nextKs = td(nextW)
      val nextKsLen = nextKs.length

      var j = 0
      while (j < curWKsLen) {
        val k = curWKs(j)
        val logValue = new Array[Double](nextKsLen)
        var l = 0
        while (l < nextKsLen) {
          val k2 = nextKs(l)
          logValue(l) = logTr(k)(k2) + logEm(k2)(nextW) + nextLogBkd(k2)
          l += 1
        }
        curLogBkd(k) = logSum(logValue, nextKsLen)
        j += 1
      }
      //println(f"$i%3d: " + curLogBkd.zipWithIndex.map { case (v, k) => if (td(w(i)).contains(k)) exp(v).toString else "" }.map(_.padRight(30)).mkString(" "))
      i -= 1
    }
    logBkd
  }

  private[this] def contributeExpectedTrCounts(
    expectedTrCounts: Array[Array[Double]],
    w: Array[Int],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]],
    logFwd: Array[Array[Double]], logFwdP: Double,
    logBkd: Array[Array[Double]], logBkdP: Double) = {
    var i = 0
    while (i < w.length - 1) {

      val curW = w(i)
      val curWKs = td(curW)
      val curWKsLen = curWKs.length
      val nextW = w(i + 1)
      val nextWKs = td(nextW)
      val nextWKsLen = nextWKs.length

      var j = 0
      while (j < curWKsLen) {
        val k1 = curWKs(j)
        val exTrK1 = expectedTrCounts(k1)
        var l = 0
        while (l < nextWKsLen) {
          val k2 = nextWKs(l)
          val logEx = logFwd(i)(k1) + logTr(k1)(k2) + logEm(k2)(nextW) + logBkd(i + 1)(k2)
          exTrK1(k2) += exp(logEx - logFwdP)
          l += 1
        }
        j += 1
      }
      i += 1
    }
    //    println((0 until numTags).flatMap(k1 => (0 until numTags).map(k2 => f"exTr($k1)($k2)=${exTr(k1)(k2)}")).mkString(" "))
  }

  private[this] def contributeExpectedEmCounts(
    expectedEmCounts: Array[Array[Double]],
    w: Array[Int],
    numWords: Int, numTags: Int,
    td: Array[Array[Int]], rtda: Array[Array[Int]],
    logTr: Array[Array[Double]], logEm: Array[Array[Double]],
    logFwd: Array[Array[Double]], logFwdP: Double,
    logBkd: Array[Array[Double]], logBkdP: Double) = {
    var i = 0
    while (i < w.length) {

      val curW = w(i)
      val curWKs = td(curW)
      val curWKsLen = curWKs.length

      var j = 0
      while (j < curWKsLen) {
        val k = curWKs(j)
        val logEx = logFwd(i)(k) + logBkd(i)(k)
        expectedEmCounts(k)(curW) += exp(logEx - logFwdP)
        //        print(f"exEm  $i%3d: " + exp(ex - logFwdP).toString.padRight(30) + " ")
        j += 1
      }
      //      println
      i += 1
    }
    //    println((1 until numTags).map(k => (1 until numWords).sumBy(exEm(k))).mkString(" "))
    //    println((0 until numTags).flatMap(k => (0 until numWords).map(w => f"exEm($k)($w)=${exEm(k)(w)}")).mkString(" "))
  }

}

object EmHmm {
  def main(args: Array[String]): Unit = {
    val (arguments, options) = CommandLineUtil.parseArgs(args)

    /*
     * OPTIONS:
     *   --lambda	smoothing lambda			Double			1.0
     *   --it 		num iterations				Int				50 		(or -1 if supervised)
     *   --memmcut 	memm feature cutoff  		Int				100
     *   --tdcut	TD proportion cutoff		Double			0.1
     *   --trinit	tr dist initializer			uniform | tdentries | tdcomplete | cheat			
     *   --eminit	tr dist initializer			uniform | crazy | cheat
     *   --tsmooth	post-em hmm tr smoother		unsmoothed | addlambda | onecount | none
     *   --esmooth	post-em hmm em smoother		unsmoothed | addlambda | onecount | none
     */

    val dir = options.get("dir").getOrElse("ccgpos")
    val raw = f"$dir/raw.txt"
    val tdFile = f"$dir/td.txt"
    val test = f"$dir/dev.txt"

    val rawData = Hmm.taggedSentencesFile(raw).map(_.map(_._1))

    //val lambda = options.get("lambda").fold(1.0)(_.toDouble)
    val maxIterations = options.get("it").fold(50)(_.toInt)
    val memmCutoff = options.get("memmcut").fold(100)(_.toInt)
    val tdcut = options.get("tdcut").fold(0.1)(_.toDouble)
    val trInitializer = options.getOrElse("trinit", "uniform") match {
      case x if x.startsWith("un") => new TrUniform[String, String]()
      case x if x.startsWith("tde") => new TrTagDictEntriesPossibilities[String, String](new AddLambdaTransitionDistributioner(0.5))
      case x if x.startsWith("tdc") => new TrTagDictCompletePossibilities[String, String](new AddLambdaTransitionDistributioner(0.1))
      case x if x.startsWith("ch") => ???
    }
    val emInitializer = options.getOrElse("eminit", "uniform") match {
      case x if x.startsWith("un") => new EmUniform[String, String]()
      case x if x.startsWith("cr") => new EmCrazy[String, String](0.05, 0.25)
      case x if x.startsWith("ch") => new EmCheat[String, String](Hmm.taggedSentencesFile(raw), 0.1)
    }
    val tsmooth: TransitionDistributioner[String, String] =
      options.getOrElse("tsmooth", "addlambda") match {
        case x if x.startsWith("un") | x.startsWith("no") => new UnsmoothedTransitionDistributioner()
        case x if x.startsWith("ad") => new AddLambdaTransitionDistributioner(0.2)
        case x if x.startsWith("on") => new OneCountTransitionDistributioner(???, ???)
      }
    val esmooth: EmissionDistributioner[String, String] =
      options.getOrElse("esmooth", "addlambda") match {
        case x if x.startsWith("un") | x.startsWith("no") => new UnsmoothedEmissionDistributioner()
        case x if x.startsWith("ad") => new AddLambdaEmissionDistributioner(0.1)
        case x if x.startsWith("on") => new OneCountEmissionDistributioner(???, ???)
      }

    //val meTrainer = new MemmTrainer()

    val tdCutoff = Some(tdcut)
    val tdFactory = new SimpleTagDictionaryFactory[String, String](tdCutoff)
    val tdData = Hmm.taggedSentencesFile(tdFile)
    val td = tdFactory(tdData, "<S>", "<S>", "<E>", "<E>")

    def printInfo() {
      println(f"tagset = $dir")
      println(f"max iterations = $maxIterations")
      println(f"memm cutoff = $memmCutoff")
      println(f"td cutoff = $tdcut")
      println(f"tr initializer = $trInitializer")
      println(f"em initializer = $emInitializer")
      println(f"tr distributioner = $tsmooth")
      println(f"em distributioner = $esmooth")
      println(f"td tokens  = ${tdData.flatten.size}%6s  (${tdData.size} sentences)")
      println(f"td words   = ${td.entries.keySet.size}%6s")
      println(f"td entries = ${td.entries.sumBy(_._2.size)}%6s")
    }
    printInfo()

    val emTrainer = new EmHmmTrainer(trInitializer, emInitializer, maxIterations, 1e-10)
    val smTrainer = new SmoothedHmmTrainer(tsmooth, esmooth, "<S>", "<S>", "<E>", "<E>", tdCutoff)
    val meTrainer = new MemmTaggerTrainer(maxIterations, memmCutoff)

    /* learn an unsmoothed HMM from EM */
    val emHmm = emTrainer.train(rawData, td)
    /* learn an MEMM from auto-tagged data produced by the EM-trained HMM */
    val memm = meTrainer.train(rawData.map(s => s zipSafe emHmm.tagSentence(s)))
    /* train a smoothed HMM from auto-tagged data produced by the EM-trained HMM */
    val smoothedHmm = smTrainer.train(rawData.map(s => s zipSafe emHmm.tagSentence(s)))
    /* learn an MEMM from auto-tagged data produced by the smoothed HMM */
    val memmFromSmoothed = meTrainer.train(rawData.map(s => s zipSafe smoothedHmm.tagSentence(s)))

    printInfo()

    val testData = Hmm.taggedSentencesFile(test)
    println("\nUnsmoothed HMM from EM")
    val a = TaggerEvaluator(emHmm, testData)
    println("\nMEMM trained from auto-tagged data produced by the EM-trained HMM")
    val b = TaggerEvaluator(memm, testData)
    println("\nHMM from smoothing auto-tagged data produced by the EM-trained HMM")
    val c = TaggerEvaluator(smoothedHmm, testData)
    println("\nMEMM trained from auto-tagged data produced by the above smoothed HMM")
    val d = TaggerEvaluator(memmFromSmoothed, testData)
    println(f"$a\t$b\t$c\t$d")
  }
}
