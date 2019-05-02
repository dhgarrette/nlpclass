package nlp.a4

import org.junit.Test
import dhg.util.TestUtil
import dhg.util.StringUtil._
import nlp.a4.EmHmmInitialization._
import math.{ log, exp, abs, pow }
import org.junit.Assert._
import Double.NaN
import nlp.a1.ProbabilityDistribution
import nlp.a1.ConditionalProbabilityDistribution

class EmHmmTests {

  @Test
  def test_toy_train_5iterations {
    val scale = 10

    val sentences = Vector(
      "a cat chases the dog",
      "the dog walks",
      "the man walks the dog",
      "the man runs").map(_.lsplit(" "))
    val tagdict = TagDictionary(Map(
      "the" -> Set("D"),
      "a" -> Set("D"),
      "every" -> Set("D"),
      "some" -> Set("D"),
      "man" -> Set("N", "V"),
      "cat" -> Set("N"),
      "bird" -> Set("N"),
      "fox" -> Set("N"),
      "walks" -> Set("V"),
      "flies" -> Set("N", "V")),
      "<S>", "<S>", "<E>", "<E>")

    val trInitializer = new TrUniform[String,String]()
    val emInitializer = new EmUniform[String,String]()

    val emt = new EmHmmTrainer(trInitializer, emInitializer, 5, 1e-10)
    val hmm = emt.train(sentences = sentences, tagdict)

    val tr = hmm.transitions
    println(tr)

    val em = hmm.emissions
    println(em)
  }

  //  @Test
  //  def test_icecream_train_5iterations {
  //    val scale = 10
  //
  //    val s1 = Vector(2, 3, 3, 2, 3, 2, 3, 2, 2, 3, 1, 3, 3, 1, 1, 1, 2, 1, 1, 1, 3, 1, 2, 1, 1, 1, 2, 3, 3, 2, 3, 2, 2)
  //    val s2 = Vector(1, 1, 2, 1, 2, 2, 1, 3, 3, 2, 3, 3, 3, 2, 1, 3, 3, 2, 1, 3, 2, 3, 2, 2, 3, 1, 2, 1, 3, 1, 1, 2, 1)
  //
  //    val tagdict = TagDictionary(Map(1 -> Set("C", "H"), 2 -> Set("C", "H")), -1, "<S>", -2, "<E>")
  //
  //    val trInitializer = new TransitionInitializer[Int, String] {
  //      override def apply(sentences: Vector[Vector[Int]], tagdict: TagDictionary[Int, String]): ConditionalProbabilityDistribution[String, String] = {
  //        new ConditionalProbabilityDistribution(Map(
  //          "<S>" -> new ProbabilityDistribution(Map("C" -> 0.35, "H" -> 0.65)),
  //          "C" -> new ProbabilityDistribution(Map("C" -> 0.40, "H" -> 0.32, "<E>" -> 0.28)),
  //          "H" -> new ProbabilityDistribution(Map("C" -> 0.25, "H" -> 0.55, "<E>" -> 0.20))))
  //      }
  //    }
  //    val emInitializer = new EmissionInitializer[Int, String] {
  //      override def apply(sentences: Vector[Vector[Int]], tagdict: TagDictionary[Int, String]): ConditionalProbabilityDistribution[String, Int] = {
  //        new ConditionalProbabilityDistribution(Map(
  //          "<S>" -> new ProbabilityDistribution(Map(-1 -> 1.0)),
  //          "C" -> new ProbabilityDistribution(Map(1 -> 0.80, 2 -> 0.15, 3 -> 0.05)),
  //          "H" -> new ProbabilityDistribution(Map(1 -> 0.13, 2 -> 0.37, 3 -> 0.50)),
  //          "<E>" -> new ProbabilityDistribution(Map(-2 -> 1.0))))
  //      }
  //    }
  //
  //    val emt = new EmHmmTrainer(trInitializer, emInitializer, 5, 1e-10)
  //    val hmm = emt.train(sentences = Vector(s1, s2), tagdict)
  //
  //    val tr = hmm.transitions
  //    assertEquals(0.665184015, tr("C", "C"), pow(0.1, scale))
  //    assertEquals(0.334815985, tr("H", "C"), pow(0.1, scale))
  //    assertEquals(0.198384244, tr("C", "H"), pow(0.1, scale))
  //    assertEquals(0.801615756, tr("H", "H"), pow(0.1, scale))
  //
  //    val em = hmm.emissions
  //    assertEquals(0.0, em(-1, "C"), pow(0.1, scale))
  //    assertEquals(0.754096378, em(1, "C"), pow(0.1, scale))
  //    assertEquals(0.156358532, em(2, "C"), pow(0.1, scale))
  //    assertEquals(0.089545091, em(3, "C"), pow(0.1, scale))
  //    assertEquals(0.0, em(-2, "C"), pow(0.1, scale))
  //    assertEquals(0.0, em(-1, "H"), pow(0.1, scale))
  //    assertEquals(0.079861252, em(1, "H"), pow(0.1, scale))
  //    assertEquals(0.439944815, em(2, "H"), pow(0.1, scale))
  //    assertEquals(0.480193934, em(3, "H"), pow(0.1, scale))
  //    assertEquals(0.0, em(-2, "H"), pow(0.1, scale))
  //  }

  @Test
  def test_icecream_doTrain_2iterations {
    val scale = 10

    val s1 = Array(-1, 2, 3, 3, 2, 3, 2, 3, 2, 2, 3, 1, 3, 3, 1, 1, 1, 2, 1, 1, 1, 3, 1, 2, 1, 1, 1, 2, 3, 3, 2, 3, 2, 2, 0)
    val s2 = Array(-1, 2, 3, 3, 2, 3, 2, 3, 2, 2, 3, 1, 3, 3, 1, 1, 1, 2, 1, 1, 1, 3, 1, 2, 1, 1, 1, 2, 3, 3, 2, 3, 2, 2, 0)

    val trInitializer = new TrUniform[String, String]()
    val emInitializer = new EmUniform[String, String]()

    val emt = new EmHmmTrainer(trInitializer, emInitializer, 2, 1e-10)
    val (learnedTr, learnedEm) =
      emt.doTrain(
        Vector(s1, s2).map(_.map(_ + 1)),
        numWords = 5, numTags = 4,
        td = Array(0) +: Array(1) +: Array.fill(3)(Array(2, 3)),
        rtda = Array(0) +: Array(1) +: Array.fill(2)(Array(2, 3, 4)),
        Array(
          Array(0.0, 0.0, 0.5, 0.5), //  p_t(.|START)
          Array(0.0, 0.0, 0.0, 0.0), //  p_t(.|END) 
          Array(0.0, 0.1, 0.7, 0.2), //  p_t(.|C)
          Array(0.0, 0.1, 0.1, 0.8)), // p_t(.|H)
        Array(
          Array(1.0, 0.0, 0.0, 0.0, 0.0), //  p_e(.|START)
          Array(0.0, 1.0, 0.0, 0.0, 0.0), //  p_e(.|END)
          Array(0.0, 0.0, 0.7, 0.2, 0.1), //  p_e(.|C)
          Array(0.0, 0.0, 0.1, 0.3, 0.6))) // p_e(.|H)
    assertEquals2dArray(Array(
      Array(0.0, 0.0, 0.019187795922706, 0.980812204077294), //  p_t(.|START)
      Array(NaN, NaN, NaN, NaN), //  p_t(.|END) 
      Array(0.0, 0.001218263806694, 0.885892498643987, 0.112889237549318), //  p_t(.|C)
      Array(0.0, 0.052351859830148, 0.085481356704799, 0.862166783465053)), // p_t(.|H)
      learnedTr, scale)
    assertEquals2dArray(Array(
      Array(1.0, NaN, NaN, NaN, NaN), //  p_e(.|START)
      Array(NaN, 1.0, NaN, NaN, NaN), //  p_e(.|END)
      Array(NaN, NaN, 0.714543877350896, 0.168586118793123, 0.116870003855980), //  p_e(.|C)
      Array(NaN, NaN, 0.044341970791651, 0.458226316363542, 0.497431712844807)), // p_e(.|H)
      learnedEm, scale)
  }

  @Test
  def test_icecream_doTrain_11iterations {
    val scale = 3

    val s1 = Array(-1, 2, 3, 3, 2, 3, 2, 3, 2, 2, 3, 1, 3, 3, 1, 1, 1, 2, 1, 1, 1, 3, 1, 2, 1, 1, 1, 2, 3, 3, 2, 3, 2, 2, 0)
    val s2 = Array(-1, 2, 3, 3, 2, 3, 2, 3, 2, 2, 3, 1, 3, 3, 1, 1, 1, 2, 1, 1, 1, 3, 1, 2, 1, 1, 1, 2, 3, 3, 2, 3, 2, 2, 0)

    val trInitializer = new TrUniform[String, String]()
    val emInitializer = new EmUniform[String, String]()

    val emt = new EmHmmTrainer(trInitializer, emInitializer, 11, 1e-10)
    val (learnedTr, learnedEm) =
      emt.doTrain(
        Vector(s1, s2).map(_.map(_ + 1)),
        numWords = 5, numTags = 4,
        td = Array(0) +: Array(1) +: Array.fill(3)(Array(2, 3)),
        rtda = Array(0) +: Array(1) +: Array.fill(2)(Array(2, 3, 4)),
        Array(
          Array(0.0, 0.0, 0.5, 0.5), //  p_t(.|START)
          Array(0.0, 0.0, 0.0, 0.0), //  p_t(.|END) 
          Array(0.0, 0.1, 0.7, 0.2), //  p_t(.|C)
          Array(0.0, 0.1, 0.1, 0.8)), // p_t(.|H)
        Array(
          Array(1.0, 0.0, 0.0, 0.0, 0.0), //  p_e(.|START)
          Array(0.0, 1.0, 0.0, 0.0, 0.0), //  p_e(.|END)
          Array(0.0, 0.0, 0.7, 0.2, 0.1), //  p_e(.|C)
          Array(0.0, 0.0, 0.1, 0.3, 0.6))) // p_e(.|H)
    assertEquals2dArray(Array(
      Array(0.0, 0.0, 2.741248327961E-13, 0.999999999999726), //  p_t(.|START)
      Array(NaN, NaN, NaN, NaN), //  p_t(.|END) 
      Array(0.0, 1.824185761141E-14, 0.933701716226397, 0.066298283773585), //  p_t(.|C)
      Array(0.0, 0.063135967746579, 0.071833324323886, 0.865030707929535)), // p_t(.|H)
      learnedTr, scale)
    assertEquals2dArray(Array(
      Array(1.0, NaN, NaN, NaN, NaN), //  p_e(.|START)
      Array(NaN, 1.0, NaN, NaN, NaN), //  p_e(.|END)
      Array(NaN, NaN, 0.640784802222534, 0.148092649969018, 0.211122547808448), //  p_e(.|C)
      Array(NaN, NaN, 2.136834492453E-4, 0.534039193707180, 0.465747122843575)), // p_e(.|H)
      learnedEm, scale)
  }

  def assertEquals2dArray(expected: Array[Array[Double]], result: Array[Array[Double]], scale: Int) {
    //    println("\n" + g(expected, result))
    if (expected.size != result.size)
      fail("\n" + g(expected, result, scale))
    for (((a, b), i) <- (expected zip result).zipWithIndex) {
      if (a.size != b.size)
        fail("\n" + g(expected, result, scale))
      for (((x, y), j) <- (a zip b).zipWithIndex) {
        assertTrue(f"[$i,$j]: $x != $y\n" + g(expected, result, scale), x.isNaN || abs(x - y) < pow(0.1, scale))
      }
    }
  }

  def f(a: Array[Array[Double]], scale: Int) = {
    val m1 = a.map(_.map(v => if (v == NaN) "" else f"%%.${scale}f".format(v)))
    val maxl = if (scale > 0) scale + 2 else m1.flatten.map(_.length).max
    m1.map(_.map(_.padRight(maxl)).mkString("[", ", ", "]")).mkString("\n")
  }
  def g(ex: Array[Array[Double]], re: Array[Array[Double]], scale: Int) =
    sideBySideStrings(1, "expected:", f(ex, scale), "   ", "result:", f(re, scale))

}
