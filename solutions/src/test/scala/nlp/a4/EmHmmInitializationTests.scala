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

class EmHmmInitializationTests {

  @Test
  def test_tr_uniform() {
    val sentences = Vector(
      "the dog walks",
      "the man walks the dog",
      "the man runs").map(_.lsplit(" "))
    val tagdict = TagDictionary(Map(
      "the" -> Set("D"),
      "man" -> Set("N", "V"),
      "walks" -> Set("V")),
      "<S>", "<S>", "<E>", "<E>")

    val tr = new TrUniform()(sentences, tagdict)
    assertEquals(0.0, tr("<S>", "<S>"), 1e-5)
    assertEquals(1.0 / 3, tr("D", "<S>"), 1e-5)
    assertEquals(1.0 / 3, tr("N", "<S>"), 1e-5)
    assertEquals(1.0 / 3, tr("V", "<S>"), 1e-5)
    assertEquals(1.0 / 3, tr("default", "<S>"), 1e-5)
    assertEquals(0.0, tr("<E>", "<S>"), 1e-5)

    assertEquals(0.0, tr("<S>", "D"), 1e-5)
    assertEquals(1.0 / 4, tr("D", "D"), 1e-5)
    assertEquals(1.0 / 4, tr("N", "D"), 1e-5)
    assertEquals(1.0 / 4, tr("V", "D"), 1e-5)
    assertEquals(1.0 / 4, tr("default", "D"), 1e-5)
    assertEquals(1.0 / 4, tr("<E>", "D"), 1e-5)

    assertEquals(0.0, tr("<S>", "N"), 1e-5)
    assertEquals(1.0 / 4, tr("D", "N"), 1e-5)
    assertEquals(1.0 / 4, tr("N", "N"), 1e-5)
    assertEquals(1.0 / 4, tr("V", "N"), 1e-5)
    assertEquals(1.0 / 4, tr("default", "N"), 1e-5)
    assertEquals(1.0 / 4, tr("<E>", "N"), 1e-5)

    assertEquals(0.0, tr("<S>", "V"), 1e-5)
    assertEquals(1.0 / 4, tr("D", "V"), 1e-5)
    assertEquals(1.0 / 4, tr("N", "V"), 1e-5)
    assertEquals(1.0 / 4, tr("V", "V"), 1e-5)
    assertEquals(1.0 / 4, tr("default", "V"), 1e-5)
    assertEquals(1.0 / 4, tr("<E>", "V"), 1e-5)

    assertEquals(0.0, tr("<S>", "default"), 1e-5)
    assertEquals(1.0 / 4, tr("D", "default"), 1e-5)
    assertEquals(1.0 / 4, tr("N", "default"), 1e-5)
    assertEquals(1.0 / 4, tr("V", "default"), 1e-5)
    assertEquals(1.0 / 4, tr("default", "default"), 1e-5)
    assertEquals(1.0 / 4, tr("<E>", "default"), 1e-5)

    assertEquals(0.0, tr("<S>", "<E>"), 1e-5)
    assertEquals(0.0, tr("D", "<E>"), 1e-5)
    assertEquals(0.0, tr("N", "<E>"), 1e-5)
    assertEquals(0.0, tr("V", "<E>"), 1e-5)
    assertEquals(0.0, tr("default", "<E>"), 1e-5)
    assertEquals(0.0, tr("<E>", "<E>"), 1e-5)
  }

  @Test
  def test_em_uniform() {
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

    /* Words not in TD:
       *   chases
       *   dog
       *   runs
       */

    val em = new EmUniform()(sentences, tagdict)
    assertEquals(1.0, em("<S>", "<S>"), 1e-5)
    assertEquals(0.0, em("the", "<S>"), 1e-5)
    assertEquals(0.0, em("a", "<S>"), 1e-5)
    assertEquals(0.0, em("cat", "<S>"), 1e-5)
    assertEquals(0.0, em("man", "<S>"), 1e-5)
    assertEquals(0.0, em("walks", "<S>"), 1e-5)
    assertEquals(0.0, em("dog", "<S>"), 1e-5)
    assertEquals(0.0, em("runs", "<S>"), 1e-5)
    assertEquals(0.0, em("chases", "<S>"), 1e-5)
    assertEquals(0.0, em("default", "<S>"), 1e-5)
    assertEquals(0.0, em("<E>", "<S>"), 1e-5)

    assertEquals(0.0, em("<S>", "D"), 1e-5)
    assertEquals(1.0 / (4 + 3), em("the", "D"), 1e-5)
    assertEquals(1.0 / (4 + 3), em("a", "D"), 1e-5)
    assertEquals(0.0, em("cat", "D"), 1e-5)
    assertEquals(0.0, em("man", "D"), 1e-5)
    assertEquals(0.0, em("walks", "D"), 1e-5)
    assertEquals(1.0 / (4 + 3), em("dog", "D"), 1e-5)
    assertEquals(1.0 / (4 + 3), em("runs", "D"), 1e-5)
    assertEquals(1.0 / (4 + 3), em("chases", "D"), 1e-5)
    assertEquals(1.0 / (4 + 3), em("default", "D"), 1e-5)
    assertEquals(0.0, em("<E>", "D"), 1e-5)

    assertEquals(0.0, em("<S>", "N"), 1e-5)
    assertEquals(0.0, em("the", "N"), 1e-5)
    assertEquals(0.0, em("a", "N"), 1e-5)
    assertEquals(1.0 / (5 + 3), em("cat", "N"), 1e-5)
    assertEquals(1.0 / (5 + 3), em("man", "N"), 1e-5)
    assertEquals(0.0, em("walks", "N"), 1e-5)
    assertEquals(1.0 / (5 + 3), em("dog", "N"), 1e-5)
    assertEquals(1.0 / (5 + 3), em("runs", "N"), 1e-5)
    assertEquals(1.0 / (5 + 3), em("chases", "N"), 1e-5)
    assertEquals(1.0 / (5 + 3), em("default", "N"), 1e-5)
    assertEquals(0.0, em("<E>", "N"), 1e-5)

    assertEquals(0.0, em("<S>", "V"), 1e-5)
    assertEquals(0.0, em("the", "V"), 1e-5)
    assertEquals(0.0, em("a", "V"), 1e-5)
    assertEquals(0.0, em("cat", "V"), 1e-5)
    assertEquals(1.0 / (3 + 3), em("man", "V"), 1e-5)
    assertEquals(1.0 / (3 + 3), em("walks", "V"), 1e-5)
    assertEquals(1.0 / (3 + 3), em("dog", "V"), 1e-5)
    assertEquals(1.0 / (3 + 3), em("runs", "V"), 1e-5)
    assertEquals(1.0 / (3 + 3), em("chases", "V"), 1e-5)
    assertEquals(1.0 / (3 + 3), em("default", "V"), 1e-5)
    assertEquals(0.0, em("<E>", "V"), 1e-5)

    assertEquals(0.0, em("<S>", "default"), 1e-5)
    assertEquals(0.0, em("the", "default"), 1e-5)
    assertEquals(0.0, em("a", "default"), 1e-5)
    assertEquals(0.0, em("cat", "default"), 1e-5)
    assertEquals(0.0, em("man", "default"), 1e-5)
    assertEquals(0.0, em("walks", "default"), 1e-5)
    assertEquals(0.0, em("dog", "default"), 1e-5)
    assertEquals(0.0, em("runs", "default"), 1e-5)
    assertEquals(0.0, em("chases", "default"), 1e-5)
    assertEquals(0.0, em("default", "default"), 1e-5)
    assertEquals(0.0, em("<E>", "default"), 1e-5)

    assertEquals(0.0, em("<S>", "<E>"), 1e-5)
    assertEquals(0.0, em("the", "<E>"), 1e-5)
    assertEquals(0.0, em("a", "<E>"), 1e-5)
    assertEquals(0.0, em("cat", "<E>"), 1e-5)
    assertEquals(0.0, em("man", "<E>"), 1e-5)
    assertEquals(0.0, em("walks", "<E>"), 1e-5)
    assertEquals(0.0, em("dog", "<E>"), 1e-5)
    assertEquals(0.0, em("runs", "<E>"), 1e-5)
    assertEquals(0.0, em("chases", "<E>"), 1e-5)
    assertEquals(0.0, em("default", "<E>"), 1e-5)
    assertEquals(1.0, em("<E>", "<E>"), 1e-5)
  }

  @Test
  def test_tr_tagdictCompletePossibilities() {
    val sentences = Vector(
      "the dog walks", //         {S} -> {D} -> {DNV} -> {V}   -> {E}
      "the man walks the dog", // {S} -> {D} -> {NV}  -> {V}   -> {D} -> {DNV} -> {E}
      "the man runs") //          {S} -> {D} -> {NV}  -> {DNV} -> {E}
      .map(_.lsplit(" "))
    val tagdict = TagDictionary(Map(
      "the" -> Set("D"),
      "man" -> Set("N", "V"),
      "walks" -> Set("V")),
      "<S>", "<S>", "<E>", "<E>")

    /*    S   D   N   V   E
     * S      3               /3
     * D     2/3 5/3  2  2/3  /(15/3)
     * N     1/6 1/6  1  2/3  /2
     * V     7/6 1/6  1  5/3  /4
     * E     
     */

    val tr = new TrTagDictCompletePossibilities(new AddLambdaTransitionDistributioner(0.2))(sentences, tagdict)
    assertEquals(0.0, tr("<S>", "<S>"), 1e-5)
    assertEquals((3 + 0.2) / (3 + 3 * 0.2), tr("D", "<S>"), 1e-5)
    assertEquals((0 + 0.2) / (3 + 3 * 0.2), tr("N", "<S>"), 1e-5)
    assertEquals((0 + 0.2) / (3 + 3 * 0.2), tr("V", "<S>"), 1e-5)
    assertEquals((0 + 0.2) / (3 + 3 * 0.2), tr("default", "<S>"), 1e-5)
    assertEquals(0.0, tr("<E>", "<S>"), 1e-5)

    assertEquals(0.0, tr("<S>", "D"), 1e-5)
    assertEquals((2 / 3.0 + 0.2) / (15 / 3.0 + 4 * 0.2), tr("D", "D"), 1e-5)
    assertEquals((5 / 3.0 + 0.2) / (15 / 3.0 + 4 * 0.2), tr("N", "D"), 1e-5)
    assertEquals((2 / 1.0 + 0.2) / (15 / 3.0 + 4 * 0.2), tr("V", "D"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (15 / 3.0 + 4 * 0.2), tr("default", "D"), 1e-5)
    assertEquals((2 / 3.0 + 0.2) / (15 / 3.0 + 4 * 0.2), tr("<E>", "D"), 1e-5)

    assertEquals(0.0, tr("<S>", "N"), 1e-5)
    assertEquals((1 / 6.0 + 0.2) / (2 + 4 * 0.2), tr("D", "N"), 1e-5)
    assertEquals((1 / 6.0 + 0.2) / (2 + 4 * 0.2), tr("N", "N"), 1e-5)
    assertEquals((1 / 1.0 + 0.2) / (2 + 4 * 0.2), tr("V", "N"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (2 + 4 * 0.2), tr("default", "N"), 1e-5)
    assertEquals((2 / 3.0 + 0.2) / (2 + 4 * 0.2), tr("<E>", "N"), 1e-5)

    assertEquals(0.0, tr("<S>", "V"), 1e-5)
    assertEquals((7 / 6.0 + 0.2) / (4 + 4 * 0.2), tr("D", "V"), 1e-5)
    assertEquals((1 / 6.0 + 0.2) / (4 + 4 * 0.2), tr("N", "V"), 1e-5)
    assertEquals((1 / 1.0 + 0.2) / (4 + 4 * 0.2), tr("V", "V"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (4 + 4 * 0.2), tr("default", "V"), 1e-5)
    assertEquals((5 / 3.0 + 0.2) / (4 + 4 * 0.2), tr("<E>", "V"), 1e-5)

    assertEquals(0.0, tr("<S>", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("D", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("N", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("V", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("default", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("<E>", "default"), 1e-5)

    assertEquals(0.0, tr("<S>", "<E>"), 1e-5)
    assertEquals(0.0, tr("D", "<E>"), 1e-5)
    assertEquals(0.0, tr("N", "<E>"), 1e-5)
    assertEquals(0.0, tr("default", "<E>"), 1e-5)
    assertEquals(0.0, tr("V", "<E>"), 1e-5)
    assertEquals(0.0, tr("<E>", "<E>"), 1e-5)
  }

  @Test
  def test_tr_tagdictEntriesPossibilities() {
    val sentences = Vector(
      "the dog walks", //         {S} -> {D} -> { }  -> {V} -> {E}
      "the man walks the dog", // {S} -> {D} -> {NV} -> {V} -> {D} -> { } -> {E}
      "the man runs") //          {S} -> {D} -> {NV} -> { } -> {E}
      .map(_.lsplit(" "))
    val tagdict = TagDictionary(Map(
      "the" -> Set("D"),
      "man" -> Set("N", "V"),
      "walks" -> Set("V")),
      "<S>", "<S>", "<E>", "<E>")

    /*    S   D   N   V   E
     * S      3              /3
     * D          1   1      /2
     * N             0.5     /0.5
     * V      1      0.5  1  /2.5
     * E     
     */

    val tr = new TrTagDictEntriesPossibilities(new AddLambdaTransitionDistributioner(0.2))(sentences, tagdict)
    assertEquals(0.0, tr("<S>", "<S>"), 1e-5)
    assertEquals((3 + 0.2) / (3 + 3 * 0.2), tr("D", "<S>"), 1e-5)
    assertEquals((0 + 0.2) / (3 + 3 * 0.2), tr("N", "<S>"), 1e-5)
    assertEquals((0 + 0.2) / (3 + 3 * 0.2), tr("V", "<S>"), 1e-5)
    assertEquals((0 + 0.2) / (3 + 3 * 0.2), tr("default", "<S>"), 1e-5)
    assertEquals(0.0, tr("<E>", "<S>"), 1e-5)

    assertEquals(0.0, tr("<S>", "D"), 1e-5)
    assertEquals((0 + 0.2) / (2 + 4 * 0.2), tr("D", "D"), 1e-5)
    assertEquals((1 + 0.2) / (2 + 4 * 0.2), tr("N", "D"), 1e-5)
    assertEquals((1 + 0.2) / (2 + 4 * 0.2), tr("V", "D"), 1e-5)
    assertEquals((0 + 0.2) / (2 + 4 * 0.2), tr("default", "D"), 1e-5)
    assertEquals((0 + 0.2) / (2 + 4 * 0.2), tr("<E>", "D"), 1e-5)

    assertEquals(0.0, tr("<S>", "N"), 1e-5)
    assertEquals((0.0 + 0.2) / (0.5 + 4 * 0.2), tr("D", "N"), 1e-5)
    assertEquals((0.0 + 0.2) / (0.5 + 4 * 0.2), tr("N", "N"), 1e-5)
    assertEquals((0.5 + 0.2) / (0.5 + 4 * 0.2), tr("V", "N"), 1e-5)
    assertEquals((0.0 + 0.2) / (0.5 + 4 * 0.2), tr("default", "N"), 1e-5)
    assertEquals((0.0 + 0.2) / (0.5 + 4 * 0.2), tr("<E>", "N"), 1e-5)

    assertEquals(0.0, tr("<S>", "V"), 1e-5)
    assertEquals((1.0 + 0.2) / (2.5 + 4 * 0.2), tr("D", "V"), 1e-5)
    assertEquals((0.0 + 0.2) / (2.5 + 4 * 0.2), tr("N", "V"), 1e-5)
    assertEquals((0.5 + 0.2) / (2.5 + 4 * 0.2), tr("V", "V"), 1e-5)
    assertEquals((0.0 + 0.2) / (2.5 + 4 * 0.2), tr("default", "V"), 1e-5)
    assertEquals((1.0 + 0.2) / (2.5 + 4 * 0.2), tr("<E>", "V"), 1e-5)

    assertEquals(0.0, tr("<S>", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("D", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("N", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("V", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("default", "default"), 1e-5)
    assertEquals((0 / 1.0 + 0.2) / (0 + 4 * 0.2), tr("<E>", "default"), 1e-5)

    assertEquals(0.0, tr("<S>", "<E>"), 1e-5)
    assertEquals(0.0, tr("D", "<E>"), 1e-5)
    assertEquals(0.0, tr("N", "<E>"), 1e-5)
    assertEquals(0.0, tr("V", "<E>"), 1e-5)
    assertEquals(0.0, tr("default", "<E>"), 1e-5)
    assertEquals(0.0, tr("<E>", "<E>"), 1e-5)
  }

  @Test
  def test_em_crazy() {
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

    /* Words not in TD:
     *   chases	
     *   dog	
     *   runs	
     */

    /* C
     * 
     * a		1
     * cat		1
     * chases	1
     * dog		3
     * man		2
     * runs		1
     * the		5
     * walks	2
     */

    /* C_k
     *           D   N   V
     * a		 1
     * cat		     1
     * man		     1   1
     * the		 5
     * walks	         2
     */

    /* p(t|unk)
     * 
     *  	[sum_w' C_k(t,w')]  p(t)  |  |TD(t)|  p(unk|t)  |          p(t|unk)
     * D	6					6/11  |     4       4/12    |  24/(11*12) / (43/(11*12)) = 24/43  
     * N	2					2/11  |     5       5/12    |  10/(11*12) / (43/(11*12)) = 10/43
     * V	3					3/11  |     3       3/12    |   9/(11*12) / (43/(11*12)) =  9/43
     * 
     */

    /* C_u		  
     *          C(w)  |    D       N       V
     * chases	 1    |  24/43   10/43    9/43
     * dog		 3    |  72/43   30/43   27/43
     * runs		 1    |  24/43   10/43    9/43
     */

    /* C_ku totals
     *                    D       N       V
     *                  378/43  136/43  174/43
     */

    val em = new EmCrazy(0.2)(sentences, tagdict)
    assertEquals(1.0, em("<S>", "<S>"), 1e-5)
    assertEquals(0.0, em("the", "<S>"), 1e-5)
    assertEquals(0.0, em("a", "<S>"), 1e-5)
    assertEquals(0.0, em("cat", "<S>"), 1e-5)
    assertEquals(0.0, em("man", "<S>"), 1e-5)
    assertEquals(0.0, em("walks", "<S>"), 1e-5)
    assertEquals(0.0, em("dog", "<S>"), 1e-5)
    assertEquals(0.0, em("runs", "<S>"), 1e-5)
    assertEquals(0.0, em("chases", "<S>"), 1e-5)
    assertEquals(0.0, em("default", "<S>"), 1e-5)
    assertEquals(0.0, em("<E>", "<S>"), 1e-5)

    assertEquals(0.0, em("<S>", "D"), 1e-5)
    assertEquals(5.0 / (378 / 43.0), em("the", "D"), 1e-5)
    assertEquals(1.0 / (378 / 43.0), em("a", "D"), 1e-5)
    assertEquals(0.0, em("cat", "D"), 1e-5)
    assertEquals(0.0, em("man", "D"), 1e-5)
    assertEquals(0.0, em("walks", "D"), 1e-5)
    assertEquals((72 / 43.0) / (378 / 43.0), em("dog", "D"), 1e-5)
    assertEquals((24 / 43.0) / (378 / 43.0), em("runs", "D"), 1e-5)
    assertEquals((24 / 43.0) / (378 / 43.0), em("chases", "D"), 1e-5)
    assertEquals(0.0 / (378 / 43.0), em("default", "D"), 1e-5)
    assertEquals(0.0, em("<E>", "D"), 1e-5)

    assertEquals(0.0, em("<S>", "N"), 1e-5)
    assertEquals(0.0, em("the", "N"), 1e-5)
    assertEquals(0.0, em("a", "N"), 1e-5)
    assertEquals(1.0 / (136 / 43.0), em("cat", "N"), 1e-5)
    assertEquals(1.0 / (136 / 43.0), em("man", "N"), 1e-5)
    assertEquals(0.0, em("walks", "N"), 1e-5)
    assertEquals((30 / 43.0) / (136 / 43.0), em("dog", "N"), 1e-5)
    assertEquals((10 / 43.0) / (136 / 43.0), em("runs", "N"), 1e-5)
    assertEquals((10 / 43.0) / (136 / 43.0), em("chases", "N"), 1e-5)
    assertEquals(0.0 / (136 / 43.0), em("default", "N"), 1e-5)
    assertEquals(0.0, em("<E>", "N"), 1e-5)

    assertEquals(0.0, em("<S>", "V"), 1e-5)
    assertEquals(0.0, em("the", "V"), 1e-5)
    assertEquals(0.0, em("a", "V"), 1e-5)
    assertEquals(0.0, em("cat", "V"), 1e-5)
    assertEquals(1.0 / (174 / 43.0), em("man", "V"), 1e-5)
    assertEquals(2.0 / (174 / 43.0), em("walks", "V"), 1e-5)
    assertEquals((27 / 43.0) / (174 / 43.0), em("dog", "V"), 1e-5)
    assertEquals((9 / 43.0) / (174 / 43.0), em("runs", "V"), 1e-5)
    assertEquals((9 / 43.0) / (174 / 43.0), em("chases", "V"), 1e-5)
    assertEquals(0.0 / (174 / 43.0), em("default", "V"), 1e-5)
    assertEquals(0.0, em("<E>", "V"), 1e-5)

    assertEquals(0.0, em("<S>", "default"), 1e-5)
    assertEquals(0.0, em("the", "default"), 1e-5)
    assertEquals(0.0, em("a", "default"), 1e-5)
    assertEquals(0.0, em("cat", "default"), 1e-5)
    assertEquals(0.0, em("man", "default"), 1e-5)
    assertEquals(0.0, em("walks", "default"), 1e-5)
    assertEquals(0.0, em("dog", "default"), 1e-5)
    assertEquals(0.0, em("runs", "default"), 1e-5)
    assertEquals(0.0, em("chases", "default"), 1e-5)
    assertEquals(0.0, em("default", "default"), 1e-5)
    assertEquals(0.0, em("<E>", "default"), 1e-5)

    assertEquals(0.0, em("<S>", "<E>"), 1e-5)
    assertEquals(0.0, em("the", "<E>"), 1e-5)
    assertEquals(0.0, em("a", "<E>"), 1e-5)
    assertEquals(0.0, em("cat", "<E>"), 1e-5)
    assertEquals(0.0, em("man", "<E>"), 1e-5)
    assertEquals(0.0, em("walks", "<E>"), 1e-5)
    assertEquals(0.0, em("dog", "<E>"), 1e-5)
    assertEquals(0.0, em("runs", "<E>"), 1e-5)
    assertEquals(0.0, em("chases", "<E>"), 1e-5)
    assertEquals(0.0, em("default", "<E>"), 1e-5)
    assertEquals(1.0, em("<E>", "<E>"), 1e-5)
  }

}
